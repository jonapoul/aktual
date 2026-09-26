package aktual.budget.reports.vm

import kotlinx.datetime.DateTimeUnit.Companion.MONTH
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth
import kotlinx.datetime.YearMonthRange
import kotlinx.datetime.minus
import kotlinx.datetime.minusMonth
import kotlinx.datetime.minusYear
import kotlinx.datetime.monthsUntil
import kotlinx.datetime.number
import kotlinx.datetime.plus
import kotlinx.datetime.yearMonth

private const val MONTHS_PER_QUARTER = 3
private const val DEFAULT_WINDOW_MONTHS = 5

// packages/desktop-client/src/components/reports/reportRanges.ts calculateTimeRange()
internal fun resolveTimeRange(
  timeFrame: TimeFrame?,
  default: TimeFrame?,
  today: LocalDate,
  latestTransaction: LocalDate?,
): YearMonthRange {
  val current = today.yearMonth
  val start = timeFrame?.start ?: default?.start ?: current.minus(DEFAULT_WINDOW_MONTHS, MONTH)
  val end = timeFrame?.end ?: default?.end ?: current
  val mode = timeFrame?.mode ?: default?.mode ?: TimeFrameMode.SlidingWindow

  return when (mode) {
    Full -> {
      val latest = latestTransaction?.yearMonth
      start..(if (latest != null && latest > current) latest else current)
    }

    SlidingWindow -> {
      val offset = start.monthsUntil(end)
      if (start > end) current..current.plus(offset, MONTH)
      else current.minus(offset, MONTH)..current
    }

    LastMonth -> current.minusMonth().let { it..it }
    LastYear ->
      YearMonth(current.year - 1, Month.JANUARY)..YearMonth(current.year - 1, Month.DECEMBER)
    YearToDate -> YearMonth(current.year, Month.JANUARY)..current
    PriorYearToDate -> YearMonth(current.year - 1, Month.JANUARY)..current.minusYear()
    CurrentQuarter -> quarter(current)
    PreviousQuarter -> quarter(current.minus(MONTHS_PER_QUARTER, MONTH))
    Static -> start..end
  }
}

private fun quarter(month: YearMonth): YearMonthRange {
  val first =
    YearMonth(
      month.year,
      Month((month.month.number - 1) / MONTHS_PER_QUARTER * MONTHS_PER_QUARTER + 1),
    )
  return first..first.plus(MONTHS_PER_QUARTER - 1, MONTH)
}
