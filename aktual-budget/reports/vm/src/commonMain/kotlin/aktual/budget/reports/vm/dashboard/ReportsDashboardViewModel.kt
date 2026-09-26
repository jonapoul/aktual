package aktual.budget.reports.vm.dashboard

import aktual.budget.db.dao.CustomReportsDao
import aktual.budget.db.dao.DashboardDao
import aktual.budget.model.WidgetId
import aktual.budget.reports.vm.AgeOfMoneyReportMeta
import aktual.budget.reports.vm.BalanceForecastReportMeta
import aktual.budget.reports.vm.BudgetAnalysisReportMeta
import aktual.budget.reports.vm.CalendarReportMeta
import aktual.budget.reports.vm.CashFlowReportMeta
import aktual.budget.reports.vm.ChartData
import aktual.budget.reports.vm.ChartDataLoader
import aktual.budget.reports.vm.CrossoverReportMeta
import aktual.budget.reports.vm.CustomReportMeta
import aktual.budget.reports.vm.FormulaReportMeta
import aktual.budget.reports.vm.MarkdownReportMeta
import aktual.budget.reports.vm.MonteCarloReportMeta
import aktual.budget.reports.vm.NetWorthReportMeta
import aktual.budget.reports.vm.SankeyReportMeta
import aktual.budget.reports.vm.SpendingReportMeta
import aktual.budget.reports.vm.SummaryReportMeta
import aktual.budget.reports.vm.UnsupportedReportMeta
import aktual.di.BudgetScope
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@Stable
@ViewModelKey
@ContributesIntoMap(BudgetScope::class)
class ReportsDashboardViewModel
internal constructor(
  private val chartDataLoader: ChartDataLoader,
  private val dashboardDao: DashboardDao,
  private val customReportsDao: CustomReportsDao,
  private val decoder: DashboardItemDecoder,
) : ViewModel() {
  val items: StateFlow<ImmutableList<DashboardItem>> =
    dashboardDao
      .observeAll()
      .map { widgets -> widgets.mapNotNull(decoder::decode).toImmutableList() }
      .stateIn(viewModelScope, Eagerly, initialValue = persistentListOf())

  fun renameReport(item: DashboardItem, name: String) {
    viewModelScope.launch {
      when (val meta = item.meta) {
        is AgeOfMoneyReportMeta,
        is BalanceForecastReportMeta,
        is BudgetAnalysisReportMeta,
        is CalendarReportMeta,
        is CashFlowReportMeta,
        is CrossoverReportMeta,
        is FormulaReportMeta,
        is MonteCarloReportMeta,
        is NetWorthReportMeta,
        is SankeyReportMeta,
        is SpendingReportMeta,
        is SummaryReportMeta -> dashboardDao.rename(item.id, name)

        // Not nameable
        is MarkdownReportMeta,
        is UnsupportedReportMeta -> Unit

        // Named, but it's stored in a separate table
        is CustomReportMeta -> customReportsDao.rename(meta.id, name)
      }
    }
  }

  fun deleteReport(id: WidgetId) {
    viewModelScope.launch { dashboardDao.deleteById(id) }
  }

  fun observeChartData(item: DashboardItem): Flow<ChartData> = chartDataLoader.load(item.meta)
}
