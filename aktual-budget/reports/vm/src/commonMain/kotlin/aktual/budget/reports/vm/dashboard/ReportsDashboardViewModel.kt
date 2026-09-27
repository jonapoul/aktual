package aktual.budget.reports.vm.dashboard

import aktual.budget.db.dao.DashboardDao
import aktual.budget.model.DashboardPageId
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
import aktual.budget.reports.vm.DashboardSync
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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import logcat.logcat

@Stable
@ViewModelKey
@ContributesIntoMap(BudgetScope::class)
class ReportsDashboardViewModel
internal constructor(
  private val chartDataLoader: ChartDataLoader,
  private val dashboardDao: DashboardDao,
  private val sync: DashboardSync,
  private val pages: DashboardPages,
  decoder: DashboardItemDecoder,
) : ViewModel() {
  val allPages: StateFlow<ImmutableList<DashboardPage>> =
    pages.all
      .map { it.toImmutableList() }
      .stateIn(viewModelScope, Eagerly, initialValue = persistentListOf())

  // Page and items change together, so the UI never shows one page's items under another's state
  val content: StateFlow<DashboardContent> =
    pages.selected
      .flatMapLatest { page ->
        if (page == null) {
          flowOf(DashboardContent(page = null, items = persistentListOf()))
        } else {
          dashboardDao.observeByPage(page.id).map { widgets ->
            DashboardContent(page, widgets.mapNotNull(decoder::decode).toImmutableList())
          }
        }
      }
      .stateIn(viewModelScope, Eagerly, initialValue = DashboardContent(null, persistentListOf()))

  fun selectPage(id: DashboardPageId) = pages.select(id)

  fun createPage(name: String) {
    logcat.d { "Creating dashboard page $name" }
    viewModelScope.launch { pages.create(name) }
  }

  fun renamePage(id: DashboardPageId, name: String) {
    logcat.d { "Renaming dashboard page $id to $name" }
    viewModelScope.launch { pages.rename(id, name) }
  }

  fun deletePage(id: DashboardPageId) {
    logcat.d { "Deleting dashboard page $id" }
    viewModelScope.launch { pages.delete(id) }
  }

  fun renameReport(item: DashboardItem, name: String) {
    logcat.d { "Renaming report ${item.id} to $name" }
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
        is SummaryReportMeta -> sync.renameWidget(item.id, name)

        // Not nameable
        is MarkdownReportMeta,
        is UnsupportedReportMeta -> logcat.w { "Can't rename ${item.id}: $meta" }

        // Named, but it's stored in a separate table
        is CustomReportMeta -> sync.renameCustomReport(meta.id, name)
      }
    }
  }

  fun deleteReport(id: WidgetId) {
    logcat.d { "Deleting report $id" }
    viewModelScope.launch { sync.deleteWidget(id) }
  }

  fun observeChartData(item: DashboardItem): Flow<ChartData> = chartDataLoader.load(item.meta)
}
