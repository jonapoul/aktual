package aktual.budget.reports.vm.report

import aktual.budget.model.WidgetType
import aktual.budget.reports.vm.ChartData
import aktual.budget.reports.vm.dashboard.DashboardItem
import androidx.compose.runtime.Immutable

@Immutable
sealed interface ReportState {
  data object Loading : ReportState

  data object NotFound : ReportState

  data class Loaded(val type: WidgetType, val item: DashboardItem, val data: ChartData) :
    ReportState
}
