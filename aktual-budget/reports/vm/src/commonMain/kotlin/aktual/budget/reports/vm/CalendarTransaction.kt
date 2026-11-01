package aktual.budget.reports.vm

import aktual.budget.model.Amount
import aktual.budget.model.ConditionSet
import aktual.budget.model.ConditionsFilter
import aktual.budget.model.FirstDayOfWeek
import aktual.budget.model.sumOf
import kotlin.math.absoluteValue
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.datetime.YearMonthRange
import kotlinx.datetime.isoDayNumber

internal data class CalendarTransaction(val date: LocalDate, val amount: Long)

// packages/desktop-client/src/components/reports/spreadsheets/calendar-spreadsheet.ts
// recalculate()
internal fun calculateCalendar(
  title: String,
  range: YearMonthRange,
  transactions: List<CalendarTransaction>,
  firstDayOfWeek: FirstDayOfWeek,
  conditions: ConditionSet? = null,
): CalendarData {
  val byDate = transactions.groupBy { it.date }
  val weekStart = firstDayOfWeek.toDayOfWeek()
  val months = range.map { month -> calendarMonth(month, byDate, weekStart) }
  return CalendarData(
    title = title,
    start = range.start,
    end = range.endInclusive,
    income = months.sumOf { it.income },
    expenses = months.sumOf { it.expenses },
    firstDayOfWeek = weekStart,
    months = months.toImmutableList(),
    conditions = conditions?.takeIf { it.conditions.isNotEmpty() },
  )
}

// packages/desktop-client/src/components/reports/reports/Calendar.tsx: the report's own conditions,
// plus the tapped day
fun CalendarData.dayFilter(date: LocalDate): ConditionsFilter =
  ConditionsFilter(listOfNotNull(conditions, ConditionSet(dateConditions(date..date))))

// Days outside the month pad out the first and last weeks, numbered 0 so the chart leaves them
// blank
private fun calendarMonth(
  month: YearMonth,
  byDate: Map<LocalDate, List<CalendarTransaction>>,
  weekStart: DayOfWeek,
): CalendarMonth {
  val leading = (month.firstDay.dayOfWeek.isoDayNumber - weekStart.isoDayNumber).mod(DAYS_PER_WEEK)
  val trailing = (DAYS_PER_WEEK - (leading + month.numberOfDays) % DAYS_PER_WEEK) % DAYS_PER_WEEK
  val days =
    month.days.map { date ->
      val amounts = byDate[date].orEmpty().map { it.amount }
      CalendarDay(
        day = date.day,
        income = Amount(amounts.filter { it > 0 }.sum()),
        expenses = Amount(amounts.filter { it < 0 }.sum().absoluteValue),
      )
    }
  val padding = CalendarDay(day = 0, income = Zero, expenses = Zero)
  return CalendarMonth(
    income = days.sumOf { it.income },
    expenses = days.sumOf { it.expenses },
    month = month,
    days = (List(leading) { padding } + days + List(trailing) { padding }).toImmutableList(),
  )
}

private fun FirstDayOfWeek.toDayOfWeek(): DayOfWeek =
  DayOfWeek(if (value == 0) DAYS_PER_WEEK else value)
