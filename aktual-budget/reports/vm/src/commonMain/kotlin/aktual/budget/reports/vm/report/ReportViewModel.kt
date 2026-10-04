package aktual.budget.reports.vm.report

import aktual.budget.db.dao.DashboardDao
import aktual.budget.model.WidgetId
import aktual.budget.reports.vm.ChartDataLoader
import aktual.budget.reports.vm.DashboardSync
import aktual.budget.reports.vm.MonteCarloReportMeta
import aktual.budget.reports.vm.TextAlign
import aktual.budget.reports.vm.dashboard.DashboardItemDecoder
import aktual.di.BudgetScope
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import logcat.logcat

@Stable
@AssistedInject
class ReportViewModel
internal constructor(
  @Assisted private val id: WidgetId,
  dashboardDao: DashboardDao,
  private val sync: DashboardSync,
  chartDataLoader: ChartDataLoader,
  decoder: DashboardItemDecoder,
) : ViewModel() {
  val state: StateFlow<ReportState> =
    dashboardDao
      .observeById(id)
      .flatMapLatest { widget ->
        val type = widget?.type
        val item = widget?.let(decoder::decode)
        if (type == null || item == null) {
          flowOf(ReportState.NotFound)
        } else if (item.meta is MonteCarloReportMeta) {
          flowOf(ReportState.MonteCarlo)
        } else {
          chartDataLoader.load(item.meta).map { data -> ReportState.Loaded(type, item, data) }
        }
      }
      .stateIn(viewModelScope, Eagerly, initialValue = ReportState.Loading)

  fun saveText(content: String, align: TextAlign) {
    logcat.d { "Saving text for $id" }
    viewModelScope.launch { sync.setWidgetText(id, content, align) }
  }

  @AssistedFactory
  @ManualViewModelAssistedFactoryKey
  @ContributesIntoMap(BudgetScope::class)
  interface Factory : ManualViewModelAssistedFactory {
    fun create(id: WidgetId): ReportViewModel
  }
}
