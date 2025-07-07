package actual.budget.reports.vm

import actual.budget.model.Amount
import actual.budget.model.CustomReportId
import actual.budget.model.DateRangeType
import actual.budget.model.ReportDate
import androidx.compose.runtime.Immutable

@Immutable
data class ReportDashboardItem(
  val id: CustomReportId,
  val name: String,
  val range: ReportRange,
  val values: ReportValues,
  val data: ChartData,
)

@Immutable
sealed interface ReportRange {
  data class Static(val start: ReportDate, val end: ReportDate) : ReportRange
  data class Dynamic(val type: DateRangeType) : ReportRange
}

@Immutable
sealed interface ReportValues {
  data object None : ReportValues
  data class Shown(val amount: Amount, val change: Amount) : ReportValues
}
