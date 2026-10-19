package aktual.budget.reports.vm.report

import aktual.budget.model.WidgetType
import aktual.budget.reports.vm.ChartData
import aktual.budget.reports.vm.dashboard.DashboardItem
import androidx.compose.runtime.Immutable

@Immutable
sealed interface ReportState {
  data object Loading : ReportState

  data object NotFound : ReportState

  // Monte Carlo reports have their own page, with its own view model
  data object MonteCarlo : ReportState

  // So do formula cards, which switch between viewing and editing in place
  data object Formula : ReportState

  // And custom reports, which have an editor
  data object Custom : ReportState

  // And budget analysis reports, which have settings
  data object BudgetAnalysis : ReportState

  data class Loaded(val type: WidgetType, val item: DashboardItem, val data: ChartData) :
    ReportState
}
