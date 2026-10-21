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
import aktual.budget.model.DbMetadata.Companion.LastScheduleRun
import aktual.budget.model.LocalChange
import aktual.budget.model.MessageValue
import aktual.budget.model.RecurConfig
import aktual.budget.model.ScheduleId
import aktual.budget.model.SyncedPrefKey.Global.UpcomingScheduledTransactionLength
import aktual.budget.model.UpcomingLength
import aktual.budget.model.localChange
import aktual.budget.model.messageValue
import aktual.budget.model.nextDate
import aktual.core.Calendar
import aktual.di.BudgetCoroutineScope
import aktual.di.BudgetScope
import aktual.di.Initializable
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.ForScope
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import logcat.logcat

/**
 * Moves paid schedules along after a budget sync, at most once a day. Mirrors
 * advanceSchedulesService() in packages/loot-core/src/server/schedules/app.ts, without posting
 * transactions yet.
 */
@SingleIn(BudgetScope::class)
@ContributesIntoSet(BudgetScope::class, binding<@ForScope(BudgetScope::class) Initializable>())
class ScheduleAdvancer
internal constructor(
  private val scheduleDao: ScheduleDao,
  private val accountDao: AccountDao,
  private val preferencesDao: PreferencesDao,
  private val syncController: BudgetSyncController,
  private val syncEvents: SyncEvents,
  private val budgetPrefs: BudgetLocalPreferences,
  private val scope: BudgetCoroutineScope,
  private val calendar: Calendar,
) : Initializable {
  override fun initialize() {
    scope.launch { syncEvents.completed.collect(::onSyncCompleted) }
  }

  internal suspend fun onSyncCompleted(outcome: SyncOutcome) {
    val today = calendar.today()
    if (budgetPrefs[LastScheduleRun] == today) return

    // Only mark the day as done when sync succeeded, so a failed sync gets retried. This happens
    // before advancing, since syncing our own changes completes another sync
    if (outcome == Succeeded) budgetPrefs.update { it.set(LastScheduleRun, today) }

    try {
      advance(today)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      // The database closes with the budget, which can happen while this is still running
      currentCoroutineContext().ensureActive()
      logcat.e(e) { "Failed advancing schedules" }
    }
  }

  private suspend fun advance(today: LocalDate) {
    val openAccounts = accountDao.getAllActive().mapTo(mutableSetOf()) { it.id }
    val rows =
      scheduleDao.getAll().filter { row ->
        row.completed != true && row._account?.let(::AccountId) in openAccounts
      }
    val latestTxDates = scheduleDao.latestTransactionDates(rows.minFromDate(today))
    val globalLength = preferencesDao[UpcomingScheduledTransactionLength].toUpcomingLength()

    val changes = rows.flatMap { row -> advance(row, today, latestTxDates, globalLength) }
    if (changes.isNotEmpty()) {
      syncController.syncChanges(changes)
      logcat.i { "Advanced schedules: ${changes.map { it.row }.distinct()}" }
    }
  }

  private suspend fun advance(
    row: GetAllActive,
    today: LocalDate,
    latestTxDates: Map<ScheduleId, LocalDate>,
    globalLength: UpcomingLength?,
  ): List<LocalChange> {
    val nextDate = row.next_date ?: return emptyList()
    if (row.status(nextDate, today, latestTxDates, globalLength) != Paid) return emptyList()

    return when (val date = row.parseDate()) {
      is Recurring -> moveNextDate(row, nextDate, date.config.nextDate(from = today))
      is Once -> if (date.date < today) complete(row) else emptyList()
      null -> emptyList()
    }
  }

  // setNextDate() without reset or advance
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

  private sealed interface ScheduleDate {
    data class Once(val date: LocalDate) : ScheduleDate

    data class Recurring(val config: RecurConfig) : ScheduleDate
  }
}
