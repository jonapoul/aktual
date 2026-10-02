package aktual.budget.reports.vm

import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.PayeeId
import aktual.budget.model.RecurConfig
import aktual.budget.model.ScheduleId
import aktual.budget.model.adjustForWeekend
import aktual.budget.model.occurrences
import kotlin.math.floor
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.datetime.DateTimeUnit.Companion.DAY
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.yearMonth
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long

private const val MAX_OCCURRENCE_ITERATIONS = 10_000
internal const val POSTED_LOOKBACK_DAYS = 2
private const val HALF = 0.5

// A skipped weekend can pull an occurrence back by up to two days, so expand a little past the end
private const val WEEKEND_MARGIN_DAYS = 3

internal sealed interface ScheduleDate {
  data class Single(val date: LocalDate) : ScheduleDate

  data class Recurring(val config: RecurConfig) : ScheduleDate
}

internal data class ForecastSchedule(
  val id: ScheduleId,
  val nextDate: LocalDate?,
  val date: ScheduleDate,
  // Null for schedules without an account
  val account: AccountId?,
  val payee: PayeeId?,
  val amount: Long,
  // Posted transactions only count on the exact day, rather than up to two days early
  val exactDate: Boolean,
)

internal data class ForecastOccurrence(
  val scheduleId: ScheduleId,
  val account: AccountId?,
  val date: LocalDate,
  val amount: Long,
)

internal data class ForecastParams(
  val title: String?,
  val start: YearMonth,
  val end: YearMonth,
  val granularity: ForecastGranularity,
  val today: LocalDate,
)

private val lenientJson = Json { ignoreUnknownKeys = true }

// _date is either a plain "YYYY-MM-DD" or a RecurConfig JSON object
internal fun parseScheduleDate(raw: String?): ScheduleDate? {
  raw ?: return null
  return runCatching {
    if (raw.trimStart().startsWith("{")) {
      ScheduleDate.Recurring(lenientJson.decodeFromString(RecurConfig.serializer(), raw))
    } else {
      ScheduleDate.Single(LocalDate.parse(raw))
    }
  }
    .getOrNull()
}

// packages/loot-core/src/shared/schedules.ts getScheduledAmount(). _amount is either a number or
// an {"num1":..,"num2":..} object for "is between" amounts, which uses the average
internal fun parseScheduleAmount(raw: String?): Long? {
  raw ?: return null
  raw.toLongOrNull()?.let {
    return it
  }
  return runCatching {
    val obj = lenientJson.parseToJsonElement(raw).jsonObject
    val num1 = obj.getValue("num1").jsonPrimitive.long
    val num2 = obj.getValue("num2").jsonPrimitive.long
    // Math.round() rounds halves up
    floor((num1 + num2).toDouble() / 2 + HALF).toLong()
  }
    .getOrNull()
}

// packages/loot-core/src/server/forecast/forecast-schedules.ts getFutureOccurrenceDates()
internal fun futureOccurrenceDates(schedule: ForecastSchedule, end: LocalDate): List<LocalDate> =
  when (val date = schedule.date) {
    is Single -> if (date.date <= end) listOf(date.date) else emptyList()
    is Recurring -> schedule.nextDate?.let { recurringDates(date.config, it, end) }.orEmpty()
  }

private fun recurringDates(
  config: RecurConfig,
  nextDate: LocalDate,
  end: LocalDate,
): List<LocalDate> {
  val raw = config.occurrences(until = end.plus(WEEKEND_MARGIN_DAYS, DAY))

  // packages/loot-core/src/shared/schedules.ts getNextDate(): the first occurrence on or after
  // the day, or the final one when a limited schedule has run out
  fun nextOnOrAfter(day: LocalDate): LocalDate? {
    val index = raw.dates.binarySearch(day).let { if (it < 0) -it - 1 else it }
    val occurrence = raw.dates.getOrNull(index) ?: raw.dates.lastOrNull()?.takeIf { raw.exhausted }
    return occurrence?.let(config::adjustForWeekend)
  }

  val dates = mutableListOf(nextDate)
  val seen = mutableSetOf(nextDate)
  var day = nextDate
  var iterations = 0
  while (day <= end && iterations < MAX_OCCURRENCE_ITERATIONS) {
    iterations++
    val next = nextOnOrAfter(day)?.takeIf { it <= end } ?: break
    if (seen.add(next)) {
      dates += next
      day = next.plus(1, DAY)
    } else {
      day = day.plus(1, DAY)
    }
  }
  return dates
}

// packages/loot-core/src/server/forecast/forecast-schedules.ts buildFutureScheduleOccurrences().
// Rules aren't run against the simulated transactions, so they keep the schedule's own account,
// payee and amount. Transfer schedules get a mirrored leg in the other account.
internal fun buildScheduleOccurrences(
  schedules: List<ForecastSchedule>,
  end: LocalDate,
  transferAccounts: Map<PayeeId, AccountId>,
  postedDates: Map<ScheduleId, List<LocalDate>>,
): List<ForecastOccurrence> {
  val occurrences = mutableListOf<ForecastOccurrence>()
  for (schedule in schedules) {
    val posted = postedDates[schedule.id].orEmpty()
    val transferAccount =
      schedule.payee?.let(transferAccounts::get)?.takeIf {
        schedule.account != null && it != schedule.account
      }

    val dates =
      futureOccurrenceDates(schedule, end).filter { date ->
        val matchStart = if (schedule.exactDate) date else date.minus(POSTED_LOOKBACK_DAYS, DAY)
        posted.none { it in matchStart..date }
      }

    for (date in dates) {
      occurrences += ForecastOccurrence(schedule.id, schedule.account, date, schedule.amount)
      if (transferAccount != null) {
        occurrences += ForecastOccurrence(schedule.id, transferAccount, date, -schedule.amount)
      }
    }
  }
  return occurrences
}

// packages/loot-core/src/server/forecast/forecast-projection.ts projectForecastData() and
// packages/desktop-client/src/components/reports/reports/balanceForecastChartData.ts. The chart
// only shows the combined balance, so accounts are summed up front. accounts contains null when
// schedules without an account are included.
internal fun calculateBalanceForecast(
  params: ForecastParams,
  accounts: Set<AccountId?>,
  startingBalances: Map<AccountId, Long>,
  dailyTotals: Map<LocalDate, Long>,
  occurrences: List<ForecastOccurrence>,
): BalanceForecastData {
  val forecastStart = params.start.firstDay
  val forecastEnd = params.end.lastDay
  val firstForecastDate =
    if (forecastEnd < params.today) forecastStart else maxOf(forecastStart, params.today)

  val included = occurrences.filter {
    it.account in accounts && it.date in firstForecastDate..forecastEnd
  }
  val scheduledByDay =
    included.groupingBy { it.date }.fold(0L) { total, occurrence -> total + occurrence.amount }

  var balance = accounts.sumOf { account -> account?.let(startingBalances::get) ?: 0L }
  val daily = LinkedHashMap<LocalDate, Amount>()
  for (day in forecastStart..forecastEnd) {
    balance += (dailyTotals[day] ?: 0L) + (scheduledByDay[day] ?: 0L)
    daily[day] = Amount(balance)
  }

  return BalanceForecastData(
    title = params.title,
    start = params.start,
    end = params.end,
    granularity = params.granularity,
    source = Schedules,
    items = if (accounts.isEmpty()) persistentMapOf() else daily.byGranularity(params.granularity),
    today = params.today,
    scheduledCount = included.distinctBy { it.date to it.scheduleId }.size,
  )
}

// packages/loot-core/src/server/forecast/forecast-tracking-budget.ts
// projectTrackingBudgetForecast(). Each month adds its budgeted income and takes away its budgeted
// expenses, starting from the whole on-budget balance.
internal fun calculateTrackingBudgetForecast(
  params: ForecastParams,
  onBudgetBalance: Long,
  budgetedIncome: Map<YearMonth, Long>,
  budgetedExpenses: Map<YearMonth, Long>,
): BalanceForecastData {
  var balance = onBudgetBalance
  val items =
    (params.start..params.end).associate { month ->
      balance += (budgetedIncome[month] ?: 0L) - (budgetedExpenses[month] ?: 0L)
      month.firstDay to Amount(balance)
    }
  return BalanceForecastData(
    title = params.title,
    start = params.start,
    end = params.end,
    granularity = Monthly,
    source = TrackingBudget,
    items = items.toImmutableMap(),
    today = params.today,
    scheduledCount = 0,
  )
}

// Monthly points are the balance at the end of each month, keyed by its first day
private fun Map<LocalDate, Amount>.byGranularity(granularity: ForecastGranularity) =
  when (granularity) {
    Daily -> toImmutableMap()
    Monthly,
    Unknown ->
      entries
        .groupBy { it.key.yearMonth }
        .entries
        .associate { (month, days) -> month.firstDay to days.maxBy { it.key }.value }
        .toImmutableMap()
  }
