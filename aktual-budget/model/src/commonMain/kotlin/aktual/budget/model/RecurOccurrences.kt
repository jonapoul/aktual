package aktual.budget.model

import kotlinx.datetime.DateTimeUnit.Companion.DAY
import kotlinx.datetime.DateTimeUnit.Companion.MONTH
import kotlinx.datetime.DateTimeUnit.Companion.YEAR
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.yearMonth

private const val MAX_RECUR_PERIODS = 100_000
private const val DAYS_PER_WEEK = 7

// How far past the start to look for occurrences, widening once for sparse configs (e.g. every 10
// years)
private val SEARCH_HORIZON_YEARS = listOf(2, 100)

data class RecurOccurrences(val dates: List<LocalDate>, val exhausted: Boolean)

// packages/loot-core/src/shared/schedules.ts recurConfigToRSchedule(), expanded the way rSchedule
// does: occurrences on or after the start date, dates that don't exist in a period are skipped,
// and the end date is inclusive. Monthly configs with both day and weekday patterns are two rules,
// each with its own count.
fun RecurConfig.occurrences(until: LocalDate): RecurOccurrences {
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
fun RecurConfig.adjustForWeekend(date: LocalDate): LocalDate {
  val saturday = date.dayOfWeek == SATURDAY
  val sunday = date.dayOfWeek == SUNDAY
  return when {
    skipWeekend != true || !(saturday || sunday) -> date
    weekendSolveMode == Before -> date.minus(if (saturday) 1 else 2, DAY)
    else -> date.plus(if (saturday) 2 else 1, DAY)
  }
}

// packages/loot-core/src/shared/schedules.ts getNextDate(): the first occurrence on or after the
// day, or the final one when a limited schedule has run out, moved off a weekend if configured
fun RecurConfig.nextDate(from: LocalDate): LocalDate? {
  for (years in SEARCH_HORIZON_YEARS) {
    val occurrences = occurrences(until = maxOf(from, start).plus(years, YEAR))
    val next = occurrences.dates.firstOrNull { it >= from }
    if (next != null) return adjustForWeekend(next)
    if (occurrences.exhausted) return occurrences.dates.lastOrNull()?.let(::adjustForWeekend)
  }
  return null
}

// packages/loot-core/src/server/schedules/app.ts getUpcomingDates()
fun RecurConfig.upcomingDates(from: LocalDate, count: Int): List<LocalDate> {
  for (years in SEARCH_HORIZON_YEARS) {
    val occurrences = occurrences(until = maxOf(from, start).plus(years, YEAR))
    val upcoming = occurrences.dates.filter { it >= from }
    if (upcoming.size >= count || occurrences.exhausted || years == SEARCH_HORIZON_YEARS.last()) {
      return upcoming.take(count).map(::adjustForWeekend)
    }
  }
  return emptyList()
}
