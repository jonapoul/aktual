package actual.budget.reports.vm

import actual.budget.model.CustomReportId
import androidx.compose.runtime.Immutable

@Immutable
data class ReportDashboardItem(
  val id: CustomReportId,
  val name: String,
  val data: ChartData,
)
