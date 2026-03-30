package aktual.budget.reports.vm.dashboard

import aktual.budget.db.Dashboard
import aktual.budget.db.dao.DashboardDao
import aktual.budget.model.DateRangeType
import aktual.budget.model.WidgetId
import aktual.budget.model.WidgetType
import aktual.budget.reports.vm.ChartData
import aktual.budget.reports.vm.CustomData
import aktual.budget.reports.vm.DateRangeMode
import aktual.budget.reports.vm.ReportTimeRange
import aktual.budget.reports.vm.TextData
import aktual.core.di.BudgetGraphHolder
import alakazam.kotlin.CoroutineContexts
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.molecule.RecompositionMode.Immediate
import app.cash.molecule.launchMolecule
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import logcat.logcat

@ViewModelKey
@ContributesIntoMap(AppScope::class)
class ReportsDashboardViewModel(budgetGraphHolder: BudgetGraphHolder, contexts: CoroutineContexts) :
  ViewModel() {
  private val graph = budgetGraphHolder.require()
  private val dao = DashboardDao(graph.database, contexts)
  private val loader = ChartDataLoader(graph.database)

  private val chartDataMap = MutableStateFlow(persistentMapOf<WidgetId, ChartData>())

  val state: StateFlow<DashboardState> =
    viewModelScope.launchMolecule(Immediate) {
      val widgets by dao.observeAll().collectAsState(initial = null)
      val loadedData by chartDataMap.collectAsState()
      buildState(widgets, loadedData)
    }

  init {
    logcat.d { "init" }

    viewModelScope.launch {
      dao.observeAll().collectLatest { widgets ->
        val loaded = widgets.associate { widget ->
          val config = parseWidgetConfig(widget.meta)
          val data = loadChartData(widget.type, config)
          widget.id to data
        }

        chartDataMap.update { loaded.toPersistentMap() }
      }
    }
  }

  @Suppress("unused")
  fun renameReport(id: WidgetId) {
    // TODO
  }

  @Suppress("unused")
  fun deleteReport(id: WidgetId) {
    // TODO
  }

  private suspend fun loadChartData(type: WidgetType?, config: WidgetConfig): ChartData {
    val title = config.name.orEmpty()
    val range = config.dateRange
    return when (type) {
      WidgetType.CashFlow -> loader.loadCashFlow(title, range)
      WidgetType.NetWorth -> loader.loadNetWorth(title, range)
      WidgetType.Calendar -> loader.loadCalendar(title, range)
      WidgetType.Summary -> loader.loadSummary(title, range, config.summaryType)
      WidgetType.Markdown -> TextData(config.markdownContent.orEmpty())
      WidgetType.Custom ->
        CustomData(title, DateRangeMode.Live, ReportTimeRange.Relative(DateRangeType.Last6Months))
      WidgetType.Spending -> TextData("") // TODO: requires comparison logic
      WidgetType.BudgetAnalysis -> TextData("") // TODO
      null -> TextData("")
    }
  }
}

private fun buildState(
  widgets: List<Dashboard>?,
  loadedData: Map<WidgetId, ChartData>,
): DashboardState {
  if (widgets == null) return DashboardState.Loading
  if (widgets.isEmpty()) return DashboardState.Empty
  val items = widgets.map { widget ->
    ReportDashboardItem(
      id = widget.id,
      type = widget.type,
      data = loadedData[widget.id] ?: TextData(""),
    )
  }
  return DashboardState.Loaded(items.toImmutableList())
}
