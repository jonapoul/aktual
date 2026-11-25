package aktual.budget.reports.vm

import aktual.budget.rules.domain.TransactionFilter
import kotlinx.datetime.LocalDateRange
import kotlinx.datetime.minus

// packages/desktop-client/src/components/reports/spreadsheets/custom-spreadsheet.ts: what a
// report's averages divide by. With a date condition, only the intervals the filter allows a day in
internal fun averagedIntervalCount(
  config: CustomReportConfig,
  range: LocalDateRange,
  intervals: List<CustomInterval>,
  filter: TransactionFilter,
): Int {
  val hasDateFilter =
    config.interval != Unknown &&
      config.balanceType != Budgeted &&
      config.conditions.any { it.customName == null && it.field == Date }
  return if (hasDateFilter) {
    countFilteredIntervals(customReportQuery(config.interval, range), intervals, filter)
  } else {
    intervals.size
  }
}

// packages/desktop-client/src/components/reports/spreadsheets/countFilteredIntervals.ts: the
// intervals with a day the filter's date conditions allow, among the days the report is queried for
internal fun countFilteredIntervals(
  query: CustomReportQuery,
  intervals: List<CustomInterval>,
  filter: TransactionFilter,
): Int =
  intervals.withIndex().count { (index, interval) ->
    val from = maxOf(interval.start, query.start)
    val to = intervals.getOrNull(index + 1)?.start?.minus(1, DAY)?.let { minOf(it, query.end) }
    (from..(to ?: query.end)).any(filter::couldMatchOn)
  }
