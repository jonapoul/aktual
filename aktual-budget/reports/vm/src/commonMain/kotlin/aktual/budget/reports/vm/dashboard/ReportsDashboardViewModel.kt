package aktual.budget.reports.vm.dashboard

import aktual.budget.db.Dashboard
import aktual.budget.db.dao.CustomReportsDao
import aktual.budget.db.dao.DashboardDao
import aktual.budget.model.WidgetId
import aktual.budget.model.WidgetType
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
import aktual.budget.reports.vm.ReportMeta
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
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import logcat.logcat

@Stable
@ViewModelKey
@ContributesIntoMap(BudgetScope::class)
class ReportsDashboardViewModel
internal constructor(
  private val chartDataLoader: ChartDataLoader,
  private val dashboardDao: DashboardDao,
  private val customReportsDao: CustomReportsDao,
) : ViewModel() {
  val items: StateFlow<ImmutableList<DashboardItem>> =
    dashboardDao
      .observeAll()
      .map { widgets -> widgets.mapNotNull(::dashboardItem).toImmutableList() }
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

  fun observeChartData(item: DashboardItem): Flow<ChartData> =
    when (val meta = item.meta) {
      is AgeOfMoneyReportMeta -> chartDataLoader.ageOfMoney(meta)
      is CashFlowReportMeta -> chartDataLoader.cashFlow(meta)
      is CrossoverReportMeta -> chartDataLoader.crossover(meta)
      is MarkdownReportMeta -> chartDataLoader.text(meta)
      is NetWorthReportMeta -> chartDataLoader.netWorth(meta)
      is BalanceForecastReportMeta,
      is BudgetAnalysisReportMeta,
      is CalendarReportMeta,
      is CustomReportMeta,
      is FormulaReportMeta,
      is MonteCarloReportMeta,
      is SankeyReportMeta,
      is SpendingReportMeta,
      is SummaryReportMeta,
      is UnsupportedReportMeta -> chartDataLoader.unsupported(meta, ReportType)
    }

  private fun dashboardItem(widget: Dashboard): DashboardItem? {
    val type = widget.type ?: return null
    // Upstream adds widgets with null meta, and each card falls back to its defaults
    val meta = widget.meta ?: JsonObject(emptyMap())
    return DashboardItem(
      id = widget.id,
      width = widget.width?.toInt() ?: 0,
      height = widget.height?.toInt() ?: 0,
      x = widget.x?.toInt() ?: 0,
      y = widget.y?.toInt() ?: 0,
      meta = decodeMeta(type, meta),
    )
  }

  // Decoding can fail on corrupt data or an upstream schema we don't model yet. Rather than let a
  // single bad widget take down the whole dashboard, fall back to an UnsupportedReportMeta
  // sentinel.
  private fun decodeMeta(type: WidgetType, meta: JsonObject): ReportMeta =
    try {
      Json.decodeFromJsonElement(ReportMeta.serializer(type), meta)
    } catch (e: Exception) {
      logcat.e(e) { "Failed to deserialize $type report meta: $meta" }
      UnsupportedReportMeta(type, meta, reason = e.message ?: e.toString())
    }
}
