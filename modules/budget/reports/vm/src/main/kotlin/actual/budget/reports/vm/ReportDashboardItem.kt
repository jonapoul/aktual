package actual.budget.reports.vm

import actual.budget.model.WidgetId
import androidx.compose.runtime.Immutable

@Immutable
data class ReportDashboardItem(
  val id: WidgetId,
  val name: String,
  val data: ChartData,
)
