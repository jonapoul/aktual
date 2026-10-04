package aktual.budget.schedules.domain

import aktual.budget.db.dao.AccountDao
import aktual.budget.db.dao.PayeeDao
import aktual.budget.db.dao.PreferencesDao
import aktual.budget.db.dao.ScheduleDao
import aktual.budget.db.schedules.GetAllActive
import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.AmountOperator
import aktual.budget.model.Operator
import aktual.budget.model.Operator.IsApprox
import aktual.budget.model.PayeeId
import aktual.budget.model.RecurConfig
import aktual.budget.model.ScheduleId
import aktual.budget.model.SyncedPrefKey.Global.UpcomingScheduledTransactionLength
import aktual.budget.model.UpcomingLength
import aktual.budget.model.upcomingDays
import aktual.core.Calendar
import dev.zacsweers.metro.Inject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import logcat.logcat

@Inject
class SchedulesLoader(
  private val scheduleDao: ScheduleDao,
  private val accountDao: AccountDao,
  private val payeeDao: PayeeDao,
  private val preferencesDao: PreferencesDao,
  private val calendar: Calendar,
) {
  suspend fun load(): ImmutableList<Schedule> {
    val rows = scheduleDao.getAll()
    val latestTxDates = scheduleDao.latestTransactionDates(rows.minFromDate())
    val globalLength = preferencesDao[UpcomingScheduledTransactionLength].toUpcomingLength()
    return toSchedules(rows, latestTxDates, globalLength)
  }

  fun observe(): Flow<ImmutableList<Schedule>> =
    scheduleDao
      .observeAll()
      .flatMapLatest { rows ->
        combine(
          scheduleDao.observeLatestTransactionDates(rows.minFromDate()),
          preferencesDao.observe(UpcomingScheduledTransactionLength),
        ) { latestTxDates, globalLength ->
          toSchedules(rows, latestTxDates, globalLength.toUpcomingLength())
        }
      }
      .distinctUntilChanged()

  suspend fun load(id: ScheduleId): Schedule? {
    val today = calendar.today()
    val row = scheduleDao[id] ?: return null
    val payeeNames = payeeDao.getAllActive().associate { it.id to it.name }
    val accountNames = accountDao.nameMap()
    val fromDate = row.next_date?.minus(value = 2, unit = DAY) ?: today
    val latestTxDates = scheduleDao.latestTransactionDates(fromDate)
    val globalLength = preferencesDao[UpcomingScheduledTransactionLength].toUpcomingLength()
    return toSchedule(row, today, payeeNames, accountNames, latestTxDates, globalLength)
  }

  // Fetch transactions for all schedules in one query using the earliest possible fromDate
  private fun List<GetAllActive>.minFromDate(): LocalDate =
    mapNotNull { it.next_date }.minOrNull()?.minus(value = 2, unit = DAY) ?: calendar.today()

  private suspend fun toSchedules(
    rows: List<GetAllActive>,
    latestTxDates: Map<ScheduleId, LocalDate>,
    globalLength: UpcomingLength?,
  ): ImmutableList<Schedule> {
    val today = calendar.today()
    val payeeNames = payeeDao.getAllActive().associate { it.id to it.name }
    val accountNames = accountDao.nameMap()
    return rows
      .mapNotNull { toSchedule(it, today, payeeNames, accountNames, latestTxDates, globalLength) }
      .sortedWith(compareBy({ it.nextDate }, { it.status.ordinal }))
      .toImmutableList()
  }

  @Suppress("ReturnCount", "LongParameterList")
  private fun toSchedule(
    row: GetAllActive,
    today: LocalDate,
    payeeNames: Map<PayeeId, String?>,
    accountNames: Map<AccountId, String?>,
    latestTxDates: Map<ScheduleId, LocalDate>,
    globalLength: UpcomingLength?,
  ): Schedule? {
    val nextDate = row.next_date ?: return null
    val ruleId = row.rule ?: return null
    val payeeId = row._payee ?: return null
    val accountId = row._account?.let { AccountId(it) } ?: return null

    val payeeName = payeeNames[payeeId] ?: return null
    val accountName = accountNames[accountId].orEmpty()

    val amount = row._amount?.toLongOrNull()?.let { Amount(it) } ?: Zero
    val amountOp =
      row._amountOp?.let { runCatching { Operator.parse(it) as? AmountOperator }.getOrNull() }
        ?: IsApprox

    // Fixed date (op = 'is'): check from next_date; recurring: look back 2 days
    val dateCondOp = row._conditions?.firstOrNull { it.field == Date }?.operator
    val txFromDate = if (dateCondOp == Is) nextDate else nextDate.minus(value = 2, unit = DAY)
    val hasTransaction = latestTxDates[row.id]?.let { it >= txFromDate } == true

    val customUpcomingLength = row.custom_upcoming_length

    return Schedule(
      id = row.id,
      name = row.name,
      ruleId = ruleId,
      nextDate = nextDate,
      isCompleted = row.completed == true,
      postsTransaction = row.posts_transaction == true,
      customUpcomingLength = customUpcomingLength,
      payeeId = payeeId,
      payeeName = payeeName,
      accountId = accountId,
      accountName = accountName,
      amount = amount,
      amountOp = amountOp,
      date = parseDateField(row._date, nextDate),
      status =
        scheduleStatus(
          nextDate = nextDate,
          isCompleted = row.completed == true,
          hasTransaction = hasTransaction,
          upcomingLength = customUpcomingLength ?: globalLength ?: DefaultUpcomingLength,
          today = today,
        ),
    )
  }

  // Mirrors getStatus() in packages/loot-core/src/shared/schedules.ts
  private fun scheduleStatus(
    nextDate: LocalDate,
    isCompleted: Boolean,
    hasTransaction: Boolean,
    upcomingLength: UpcomingLength,
    today: LocalDate,
  ): ScheduleStatus {
    val upcomingDays = upcomingLength.upcomingDays(today)
    return when {
      isCompleted -> Completed
      hasTransaction -> Paid
      nextDate == today -> Due
      nextDate > today && nextDate <= today.plus(value = upcomingDays, unit = DAY) -> Upcoming
      nextDate < today -> Missed
      else -> Scheduled
    }
  }

  // _date is either a plain "YYYY-MM-DD" or a RecurConfig JSON object {"start":"YYYY-MM-DD",...}
  private fun parseDateField(raw: String?, fallback: LocalDate): LocalDate {
    raw ?: return fallback
    return when (val element = Json.parseToJsonElement(raw)) {
      is JsonNull,
      is JsonArray -> {
        logcat.e { "Got unexpected JSON data format in $element" }
        fallback
      }

      is JsonObject -> {
        Json.decodeFromJsonElement(RecurConfig.serializer(), element).start
      }

      is JsonPrimitive -> {
        LocalDate.parse(element.content)
      }
    }
  }
}

// DEFAULT_UPCOMING_SCHEDULE_DAYS in packages/loot-core/src/shared/schedules.ts
val DefaultUpcomingLength: UpcomingLength = UpcomingLength.Days(count = 7)

private fun String?.toUpcomingLength(): UpcomingLength? =
  this?.let { runCatching { UpcomingLength.decode(it) }.getOrNull() }

// Missed, due and upcoming schedules up to the end of the window, soonest first
fun List<Schedule>.upcoming(today: LocalDate, length: UpcomingLength): ImmutableList<Schedule> {
  val end = today.plus(value = length.upcomingDays(today), unit = DAY)
  return filter { it.status in UpcomingStatuses && it.nextDate <= end }
    .sortedBy { it.nextDate }
    .toImmutableList()
}

private val UpcomingStatuses = setOf<ScheduleStatus>(Missed, Due, Upcoming)
