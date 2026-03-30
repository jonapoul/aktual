package aktual.budget.reports.vm.dashboard

import aktual.budget.model.WidgetId
import aktual.budget.model.WidgetType
import aktual.budget.reports.vm.ChartData
import androidx.compose.runtime.Immutable

@Immutable
data class ReportDashboardItem(val id: WidgetId, val type: WidgetType?, val data: ChartData)
