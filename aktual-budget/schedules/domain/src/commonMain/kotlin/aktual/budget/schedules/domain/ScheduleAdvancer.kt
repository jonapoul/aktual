package aktual.budget.schedules.domain

import aktual.budget.BudgetLocalPreferences
import aktual.budget.BudgetSyncController
import aktual.budget.SyncEvents
import aktual.budget.SyncOutcome
import aktual.budget.db.dao.AccountDao
import aktual.budget.db.dao.DatabaseTables.SCHEDULES
import aktual.budget.db.dao.DatabaseTables.SCHEDULES_NEXT_DATE
import aktual.budget.db.dao.PreferencesDao
import aktual.budget.db.dao.ScheduleDao
import aktual.budget.db.schedules.GetAllActive
import aktual.budget.model.AccountId
import aktual.budget.model.DbMetadataKey.Companion.LastScheduleRun
import aktual.budget.model.LocalChange
import aktual.budget.model.MessageValue
import aktual.budget.model.RecurConfig
import aktual.budget.model.ScheduleId
import aktual.budget.model.SyncedPrefKey.Global.UpcomingScheduledTransactionLength
import aktual.budget.model.UpcomingLength
import aktual.budget.model.localChange
import aktual.budget.model.messageValue
import aktual.budget.model.nextDate
import aktual.budget.model.nextDateAfter
import aktual.core.Calendar
import aktual.core.runCatchingCancellable
import aktual.di.BudgetCoroutineScope
import aktual.di.BudgetScope
import aktual.di.Initializable
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.ForScope
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import logcat.logcat

/**
 * Moves schedules along after a budget sync, at most once a day: posts the transactions of due and
 * missed schedules that post them, then moves paid schedules on. Mirrors advanceSchedulesService()
 * in packages/loot-core/src/server/schedules/app.ts.
 */
@SingleIn(BudgetScope::class)
@ContributesBinding(BudgetScope::class, binding<UnpostedSchedules>())
@ContributesIntoSet(BudgetScope::class, binding<@ForScope(BudgetScope::class) Initializable>())
class ScheduleAdvancer
internal constructor(
  private val scheduleDao: ScheduleDao,
  private val accountDao: AccountDao,
  private val preferencesDao: PreferencesDao,
  private val syncController: BudgetSyncController,
  private val syncEvents: SyncEvents,
  private val budgetPrefs: BudgetLocalPreferences,
  private val poster: SchedulePoster,
  private val scope: BudgetCoroutineScope,
  private val calendar: Calendar,
) : Initializable, UnpostedSchedules {
  private val mutex = Mutex()
  private val mutableHasUnposted = MutableStateFlow(false)
  override val hasUnposted: StateFlow<Boolean> = mutableHasUnposted.asStateFlow()
  private var dismissed = false

  override fun initialize() {
    scope.launch { syncEvents.completed.collect(::onSyncCompleted) }
  }

  override fun post() {
    mutableHasUnposted.update { false }
    scope.launch { tryAdvance(calendar.today(), canPost = true) }
  }

  override fun dismiss() {
    dismissed = true
    mutableHasUnposted.update { false }
  }

  internal suspend fun onSyncCompleted(outcome: SyncOutcome) {
    val today = calendar.today()
    if (budgetPrefs[LastScheduleRun] == today) return

    val result = tryAdvance(today, canPost = outcome == Succeeded) ?: return
    mutableHasUnposted.update { result.unposted && !dismissed }
    // Only mark the day as done when sync and advancing succeeded, so a failure gets retried. Sync
    // events are collected one at a time, so the one our own changes cause waits for this
    if (outcome == Succeeded) budgetPrefs.update { it.set(LastScheduleRun, today) }
  }

  private suspend fun tryAdvance(today: LocalDate, canPost: Boolean): Run? =
    runCatchingCancellable {
      mutex.withLock { advance(today, canPost) }
    }
    .getOrElse { e ->
      // The database closes with the budget, which can happen while this is still running
      currentCoroutineContext().ensureActive()
      logcat.e(e) { "Failed advancing schedules" }
      null
    }

  private suspend fun advance(today: LocalDate, canPost: Boolean): Run {
    val openAccounts = accountDao.getAllActive().mapTo(mutableSetOf()) { it.id }
    val rows =
      scheduleDao.getAll().filter { row ->
        row.completed != true && row._account?.let(::AccountId) in openAccounts
      }
    val run =
      Run(
        today = today,
        canPost = canPost,
        posting = poster.batch(),
        latestTxDates = scheduleDao.latestTransactionDates(rows.minFromDate(today)).toMutableMap(),
        globalLength = preferencesDao[UpcomingScheduledTransactionLength].toUpcomingLength(),
      )

    val changes = rows.flatMap { row -> advance(row, run) }
    run.posting.post()
    if (changes.isNotEmpty()) {
      syncController.syncChanges(changes)
      logcat.i { "Advanced schedules: ${changes.map { it.row }.distinct()}" }
    }
    return run
  }

  private suspend fun advance(row: GetAllActive, run: Run): List<LocalChange> {
    val nextDate = row.next_date ?: return emptyList()
    val status = run.status(row, nextDate)
    val date = row.parseDate()
    val recurring = (date as? Recurring)?.config

    val posts = row.posts_transaction == true && (status != Paid || recurring != null)
    if (posts && status in Postable) return postDue(row, nextDate, status, recurring, run)
    if (status != Paid) return emptyList()

    return when (date) {
      is Recurring -> moveNextDate(row, nextDate, date.config.nextDate(from = run.today))
      is Once -> if (date.date < run.today) complete(row) else emptyList()
      null -> emptyList()
    }
  }

  // Posts the schedule's transaction for each date until it's caught up, stopping at today so
  // today's isn't skipped
  private suspend fun postDue(
    row: GetAllActive,
    nextDate: LocalDate,
    status: ScheduleStatus,
    recurring: RecurConfig?,
    run: Run,
  ): List<LocalChange> {
    var date = nextDate
    var current = status
    while (current in Postable) {
      date = postAndMoveOn(row, date, current, recurring, run) ?: break
      current = run.status(row, date)
    }
    return moveNextDate(row, nextDate, date)
  }

  // Posts for [date] unless it's paid, then gives the date after it, or null to stop at [date]
  private suspend fun postAndMoveOn(
    row: GetAllActive,
    date: LocalDate,
    status: ScheduleStatus,
    recurring: RecurConfig?,
    run: Run,
  ): LocalDate? {
    if (status == Paid) {
      if (date == run.today) return null
    } else {
      if (!run.canPost) {
        logcat.w { "Not posting schedule ${row.id}, sync failed" }
        run.unposted = true
        return null
      }
      run.addTransaction(row, date)
      if (status == Due) return null
    }
    return recurring?.nextDateAfter(date)
  }

  // postTransactionForSchedule(), with the rules run as addTransactions() does
  private suspend fun Run.addTransaction(row: GetAllActive, date: LocalDate) {
    val transaction = row.toRuleTransaction(date) ?: return
    val result = posting.add(transaction) ?: return
    val schedule = result.schedule ?: return
    latestTxDates[schedule] = maxOf(latestTxDates[schedule] ?: result.date, result.date)
  }

  // setNextDate() without reset
  private suspend fun moveNextDate(
    row: GetAllActive,
    nextDate: LocalDate,
    newNextDate: LocalDate?,
  ): List<LocalChange> {
    if (newNextDate == null || newNextDate == nextDate) return emptyList()
    val existing = scheduleDao.baseNextDate(row.id) ?: return emptyList()
    val id = existing.id.toString()
    val timestamp = existing.base_next_date_ts?.toEpochMilliseconds()
    return listOf(
      LocalChange(SCHEDULES_NEXT_DATE, id, "local_next_date", newNextDate.messageValue()),
      LocalChange(
        SCHEDULES_NEXT_DATE,
        id,
        "local_next_date_ts",
        timestamp?.let(MessageValue::Number) ?: MessageValue.Null,
      ),
    )
  }

  private fun complete(row: GetAllActive): List<LocalChange> =
    listOf(localChange(SCHEDULES, row.id.toString(), "completed", true))

  // _date is either a plain "YYYY-MM-DD" or a RecurConfig JSON object. A corrupted one is skipped,
  // as upstream does when it can't find the rule
  private fun GetAllActive.parseDate(): ScheduleDate? {
    val raw = _date ?: return null
    return runCatching {
      when (val element = Json.parseToJsonElement(raw)) {
        is JsonObject ->
          ScheduleDate.Recurring(Json.decodeFromJsonElement(RecurConfig.serializer(), element))
        is JsonNull,
        is JsonArray -> null
        is JsonPrimitive -> ScheduleDate.Once(LocalDate.parse(element.content))
      }
    }
      .onFailure { logcat.w(it) { "Couldn't parse date of schedule $id: $raw" } }
      .getOrNull()
  }

  // State of one run. Posted transactions only go out at the end, so the transaction dates are
  // kept up to date here as each one is posted
  private class Run(
    val today: LocalDate,
    val canPost: Boolean,
    val posting: SchedulePoster.Batch,
    val latestTxDates: MutableMap<ScheduleId, LocalDate>,
    val globalLength: UpcomingLength?,
  ) {
    var unposted = false

    fun status(row: GetAllActive, nextDate: LocalDate): ScheduleStatus =
      row.status(nextDate, today, latestTxDates, globalLength)
  }

  private sealed interface ScheduleDate {
    data class Once(val date: LocalDate) : ScheduleDate

    data class Recurring(val config: RecurConfig) : ScheduleDate
  }

  private companion object {
    val Postable = setOf<ScheduleStatus>(Paid, Due, Missed)
  }
}
