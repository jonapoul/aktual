package actual.budget.reports.ui.charts

import actual.budget.reports.vm.CustomData
import actual.budget.reports.vm.DateRangeMode
import actual.budget.reports.vm.ReportTimeRange
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth
import kotlinx.datetime.YearMonthRange

internal object PreviewCustom {
  val DATA = CustomData(
    title = "My Custom Report",
    mode = DateRangeMode.Live,
    range = ReportTimeRange.Specific(
      range = YearMonthRange(
        start = YearMonth(2011, Month.SEPTEMBER),
        endInclusive = YearMonth(2025, Month.JULY),
      ),
    ),
  )
}
