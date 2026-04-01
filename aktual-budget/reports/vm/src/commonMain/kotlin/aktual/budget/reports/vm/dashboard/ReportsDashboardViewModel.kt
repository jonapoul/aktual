package aktual.budget.reports.vm.dashboard

import aktual.budget.db.Dashboard
import aktual.budget.db.dao.CustomReportsDao
import aktual.budget.db.dao.DashboardDao
import aktual.budget.model.WidgetId
import aktual.budget.reports.vm.BudgetAnalysisReportMeta
import aktual.budget.reports.vm.CalendarReportMeta
import aktual.budget.reports.vm.CashFlowReportMeta
import aktual.budget.reports.vm.ChartData
import aktual.budget.reports.vm.ChartDataLoader
import aktual.budget.reports.vm.CustomReportMeta
import aktual.budget.reports.vm.FormulaReportMeta
import aktual.budget.reports.vm.MarkdownReportMeta
import aktual.budget.reports.vm.NetWorthReportMeta
import aktual.budget.reports.vm.ReportMeta
import aktual.budget.reports.vm.SpendingReportMeta
import aktual.budget.reports.vm.SummaryReportMeta
import aktual.core.di.BudgetGraphHolder
import alakazam.kotlin.CoroutineContexts
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted.Companion.Eagerly
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject

@ViewModelKey
@ContributesIntoMap(AppScope::class)
class ReportsDashboardViewModel(budgetGraphHolder: BudgetGraphHolder, contexts: CoroutineContexts) :
  ViewModel() {
  private val graph = budgetGraphHolder.require()
  private val dashboardDao = DashboardDao(graph.database, contexts)
  private val customReportsDao = CustomReportsDao(graph.database, contexts)
  private val chartDataLoader = ChartDataLoader(graph.database.transactionsQueries)

  val items: StateFlow<ImmutableList<DashboardItem>> =
    dashboardDao
      .observeAll()
      .map { widgets -> widgets.mapNotNull(::dashboardItem).toImmutableList() }
      .stateIn(viewModelScope, Eagerly, initialValue = persistentListOf())

  fun renameReport(item: DashboardItem, name: String) {
    viewModelScope.launch {
      item.meta.renamed(name)?.let { meta ->
        val jsonObject = Json.encodeToJsonElement<ReportMeta>(meta).jsonObject
        dashboardDao.updateMeta(item.id, jsonObject)
      }
    }
  }

  fun deleteReport(id: WidgetId) {
    viewModelScope.launch { dashboardDao.deleteById(id) }
  }

  fun observeChartData(item: DashboardItem): Flow<ChartData> =
    when (val meta = item.meta) {
      is BudgetAnalysisReportMeta -> flowOf()
      is CalendarReportMeta -> flowOf()
      is CashFlowReportMeta -> chartDataLoader.cashFlow(meta)
      is CustomReportMeta -> flowOf()
      is FormulaReportMeta -> flowOf()
      is MarkdownReportMeta -> flowOf()
      is NetWorthReportMeta -> flowOf()
      is SpendingReportMeta -> flowOf()
      is SummaryReportMeta -> flowOf()
    }

  private fun dashboardItem(widget: Dashboard): DashboardItem? {
    val type = widget.type ?: return null
    val meta = widget.meta ?: return null
    return DashboardItem(
      id = widget.id,
      width = widget.width?.toInt() ?: 0,
      height = widget.height?.toInt() ?: 0,
      x = widget.x?.toInt() ?: 0,
      y = widget.y?.toInt() ?: 0,
      meta = Json.decodeFromJsonElement(ReportMeta.serializer(type), meta),
    )
  }

  @Suppress("BracesOnWhenStatements")
  private suspend fun ReportMeta.renamed(name: String): ReportMeta? =
    when (this) {
      // Named
      is BudgetAnalysisReportMeta -> copy(name = name)
      is CalendarReportMeta -> copy(name = name)
      is CashFlowReportMeta -> copy(name = name)
      is FormulaReportMeta -> copy(name = name)
      is NetWorthReportMeta -> copy(name = name)
      is SpendingReportMeta -> copy(name = name)
      is SummaryReportMeta -> copy(name = name)

      // Not nameable
      is MarkdownReportMeta -> null

      // Named, but it's stored in a separate table
      is CustomReportMeta -> {
        customReportsDao.rename(id, name)
        null
      }
    }
}
