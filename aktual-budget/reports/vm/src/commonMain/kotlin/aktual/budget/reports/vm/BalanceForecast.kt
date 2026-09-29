package aktual.budget.reports.vm

import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.PayeeId
import aktual.budget.model.RecurConfig
import aktual.budget.model.RecurPattern
import aktual.budget.model.RecurType
import aktual.budget.model.ScheduleId
import kotlin.math.floor
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.datetime.DateTimeUnit.Companion.DAY
import kotlinx.datetime.DateTimeUnit.Companion.MONTH
import kotlinx.datetime.DayOfWeek
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
private const val MAX_RECUR_PERIODS = 100_000
private const val POSTED_LOOKBACK_DAYS = 2
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

internal data class RecurOccurrences(val dates: List<LocalDate>, val exhausted: Boolean)

// packages/loot-core/src/shared/schedules.ts recurConfigToRSchedule(), expanded the way rSchedule
// does: occurrences on or after the start date, dates that don't exist in a period are skipped,
// and the end date is inclusive. Monthly configs with both day and weekday patterns are two rules,
// each with its own count.
internal fun RecurConfig.occurrences(until: LocalDate): RecurOccurrences {
  val interval = (interval ?: 1).coerceAtLeast(1)
  val rules: List<PeriodRule> =
    when (frequency) {
      Daily -> listOf(PeriodRule { k -> listOf(start.plus(k * interval, DAY)) })
      Weekly -> listOf(PeriodRule { k -> listOf(start.plus(k * interval * DAYS_PER_WEEK, DAY)) })
      Yearly ->
        listOf(PeriodRule { k -> listOfNotNull(dateOrNull(start.year + k * interval, start)) })
      Monthly -> monthlyRules(interval)
      Unknown -> emptyList()
    }

  var exhausted = true
  val dates = mutableSetOf<LocalDate>()
  for (rule in rules) {
    val (ruleDates, ruleExhausted) = expand(rule, until)
    dates.addAll(ruleDates)
    exhausted = exhausted && ruleExhausted
  }
  return RecurOccurrences(dates.sorted(), exhausted)
}

// The dates of the kth period of a rule, in order
private fun interface PeriodRule {
  fun dates(k: Int): List<LocalDate>
}

private fun RecurConfig.monthlyRules(interval: Int): List<PeriodRule> {
  val startMonth = start.yearMonth
  fun month(k: Int) = startMonth.plus(k * interval, MONTH)

  val patterns = patterns.orEmpty()
  if (patterns.isEmpty()) {
    return listOf(PeriodRule { k -> listOfNotNull(dateOrNull(month(k), start.day)) })
  }

  val days = patterns.filter { it.type == Day }
  val weekdays = patterns.filter { it.type != Day && it.type != Unknown }
  val dayRule = PeriodRule { k -> days.mapNotNull { dayOfMonth(month(k), it.value) }.sorted() }
  val weekdayRule = PeriodRule { k ->
    buildSet { weekdays.forEach { addAll(nthWeekday(month(k), it)) } }.sorted()
  }
  return listOfNotNull(
    dayRule.takeIf { days.isNotEmpty() },
    weekdayRule.takeIf { weekdays.isNotEmpty() },
  )
}

private fun RecurConfig.expand(
  rule: PeriodRule,
  until: LocalDate,
): Pair<List<LocalDate>, Boolean> {
  val count = endOccurrences?.takeIf { endMode == AfterNOccurrences }
  val last = endDate?.takeIf { endMode == OnDate }
  val dates = mutableListOf<LocalDate>()
  for (k in 0 until MAX_RECUR_PERIODS) {
    for (date in rule.dates(k).filter { it >= start }) {
      val exhausted =
        when {
          last != null && date > last -> true
          count != null && dates.size >= count -> true
          date > until -> false
          else -> null
        }
      if (exhausted != null) return dates to exhausted
      dates += date
    }
  }
  return dates to false
}

private fun dateOrNull(year: Int, start: LocalDate): LocalDate? = runCatching {
  LocalDate(year, start.month, start.day)
}
  .getOrNull()

private fun dateOrNull(month: YearMonth, day: Int): LocalDate? =
  if (day in 1..month.numberOfDays) LocalDate(month.year, month.month, day) else null

// Negative values count back from the end of the month, so -1 is the last day
private fun dayOfMonth(month: YearMonth, value: Int): LocalDate? =
  when {
    value > 0 -> dateOrNull(month, value)
    value < 0 -> dateOrNull(month, month.numberOfDays + value + 1)
    else -> null
  }

// "2nd Monday" or "last Friday" (-1) of the month. Zero means every one of those weekdays.
private fun nthWeekday(month: YearMonth, pattern: RecurPattern): List<LocalDate> {
  val weekday = pattern.type.dayOfWeek() ?: return emptyList()
  val all = (month.firstDay..month.lastDay).filter { it.dayOfWeek == weekday }
  val n = pattern.value
  return when {
    n > 0 -> listOfNotNull(all.getOrNull(n - 1))
    n < 0 -> listOfNotNull(all.getOrNull(all.size + n))
    else -> all
  }
}

private fun RecurType.dayOfWeek(): DayOfWeek? =
  when (this) {
    Sunday -> SUNDAY
    Monday -> MONDAY
    Tuesday -> TUESDAY
    Wednesday -> WEDNESDAY
    Thursday -> THURSDAY
    Friday -> FRIDAY
    Saturday -> SATURDAY
    Day,
    Unknown -> null
  }

// packages/loot-core/src/shared/schedules.ts getDateWithSkippedWeekend()
private fun RecurConfig.skipWeekend(date: LocalDate): LocalDate {
  val saturday = date.dayOfWeek == SATURDAY
  val sunday = date.dayOfWeek == SUNDAY
  return when {
    skipWeekend != true || !(saturday || sunday) -> date
    weekendSolveMode == Before -> date.minus(if (saturday) 1 else 2, DAY)
    else -> date.plus(if (saturday) 2 else 1, DAY)
  }
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
    return occurrence?.let(config::skipWeekend)
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

private const val DAYS_PER_WEEK = 7
