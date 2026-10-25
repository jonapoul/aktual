package aktual.budget.reports.vm.report

import aktual.budget.db.Dashboard
import aktual.budget.db.dao.DashboardDao
import aktual.budget.model.WidgetId
import aktual.budget.reports.vm.BudgetAnalysisReportMeta
import aktual.budget.reports.vm.ChartDataLoader
import aktual.budget.reports.vm.CustomReportMeta
import aktual.budget.reports.vm.DashboardSync
import aktual.budget.reports.vm.FormulaReportMeta
import aktual.budget.reports.vm.MonteCarloReportMeta
import aktual.budget.reports.vm.SummaryChartType
import aktual.budget.reports.vm.TextAlign
import aktual.budget.reports.vm.dashboard.DashboardItemDecoder
import aktual.di.BudgetScope
import aktual.di.ViewModelContext
import aktual.di.newScope
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import logcat.logcat

@Stable
@AssistedInject
@OptIn(ExperimentalCoroutinesApi::class)
class ReportViewModel
internal constructor(
  @Assisted private val id: WidgetId,
  dashboardDao: DashboardDao,
  private val sync: DashboardSync,
  private val chartDataLoader: ChartDataLoader,
  private val decoder: DashboardItemDecoder,
  viewModelContext: ViewModelContext,
) : ViewModel(viewModelContext.newScope()) {
  val state: StateFlow<ReportState> =
    dashboardDao
      .observeById(id)
      .flatMapLatest(::toReportState)
      .stateIn(viewModelScope, Eagerly, initialValue = Loading)

  fun saveText(content: String, align: TextAlign) {
    logcat.d { "Saving text for $id" }
    viewModelScope.launch { sync.setWidgetText(id, content, align) }
  }

  fun setSummaryType(type: SummaryChartType) {
    viewModelScope.launch { sync.setSummaryType(id, type) }
  }

  fun setSummaryAllTimeDivisor(allTime: Boolean) {
    viewModelScope.launch { sync.setSummaryAllTimeDivisor(id, allTime) }
  }

  private fun toReportState(widget: Dashboard?): Flow<ReportState> {
    val type = widget?.type
    val item = widget?.let(decoder::decode)
    if (type == null || item == null) return flowOf(NotFound)

    return when (val meta = item.meta) {
      is MonteCarloReportMeta -> flowOf(MonteCarlo)
      is FormulaReportMeta -> flowOf(Formula)
      is CustomReportMeta -> flowOf(Custom)
      is BudgetAnalysisReportMeta -> flowOf(BudgetAnalysis)
      else -> chartDataLoader.load(meta).map { data -> ReportState.Loaded(type, item, data) }
    }
  }

  @AssistedFactory
  @ManualViewModelAssistedFactoryKey
  @ContributesIntoMap(BudgetScope::class)
  interface Factory : ManualViewModelAssistedFactory {
    fun create(id: WidgetId): ReportViewModel
  }
}
