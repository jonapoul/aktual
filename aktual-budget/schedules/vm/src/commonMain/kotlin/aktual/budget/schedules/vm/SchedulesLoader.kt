package aktual.budget.schedules.vm

import aktual.budget.db.dao.AccountDao
import aktual.budget.db.dao.PayeeDao
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
import aktual.budget.model.UpcomingLength
import aktual.budget.model.upcomingDays
import aktual.core.Calendar
import dev.zacsweers.metro.Inject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
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
internal class SchedulesLoader(
  private val scheduleDao: ScheduleDao,
  private val accountDao: AccountDao,
  private val payeeDao: PayeeDao,
  private val calendar: Calendar,
) {
  suspend fun load(): ImmutableList<Schedule> {
    val today = calendar.today()
    val rows = scheduleDao.getAll()

    val payeeNames = payeeDao.getAllActive().associate { it.id to it.name }
    val accountNames = accountDao.nameMap()

    // Fetch transactions for all schedules in one query using the earliest possible fromDate
    val minFromDate =
      rows.mapNotNull { it.next_date }.minOrNull()?.minus(value = 2, unit = DAY) ?: today
    val latestTxDates = scheduleDao.latestTransactionDates(minFromDate)

    return rows
      .mapNotNull { row -> toSchedule(row, today, payeeNames, accountNames, latestTxDates) }
      .sortedWith(compareBy({ it.nextDate }, { it.status.ordinal }))
      .toImmutableList()
  }

  @Suppress("ReturnCount")
  private fun toSchedule(
    row: GetAllActive,
    today: LocalDate,
    payeeNames: Map<PayeeId, String?>,
    accountNames: Map<AccountId, String?>,
    latestTxDates: Map<ScheduleId, LocalDate>,
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
          customUpcomingLength = customUpcomingLength,
          today = today,
        ),
    )
  }

  // Mirrors getStatus() in packages/loot-core/src/shared/schedules.ts
  private fun scheduleStatus(
    nextDate: LocalDate,
    isCompleted: Boolean,
    hasTransaction: Boolean,
    customUpcomingLength: UpcomingLength?,
    today: LocalDate,
  ): ScheduleStatus {
    val length = customUpcomingLength ?: UpcomingLength.Days(7)
    val upcomingDays = length.upcomingDays(today)
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
