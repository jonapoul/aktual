package aktual.budget.reports.vm.dashboard

import aktual.budget.model.WidgetId
import aktual.budget.reports.vm.ReportMeta
import androidx.compose.runtime.Immutable

@Immutable
data class DashboardItem(
  val id: WidgetId,
  val x: Int,
  val y: Int,
  val meta: ReportMeta,
  val width: Int = 4,
  val height: Int = 2,
)
