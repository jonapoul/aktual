package aktual.budget.reports.vm.choosetype

import aktual.budget.model.DashboardPageId
import aktual.budget.model.WidgetId
import aktual.budget.model.WidgetType
import aktual.budget.reports.vm.DashboardSync
import aktual.core.UuidGenerator
import aktual.core.model.Empty
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import logcat.logcat

@Stable
@AssistedInject
class ChooseReportTypeViewModel
internal constructor(
  @Assisted private val page: DashboardPageId,
  private val uuidGenerator: UuidGenerator,
  private val sync: DashboardSync,
) : ViewModel() {
  private var job: Job? = null
  private val shouldNavigateChannel = Channel<ShouldNavigateEvent>()
  private val mutableDialog = MutableStateFlow<ChooseReportTypeDialog?>(null)

  val shouldNavigateEvent: Flow<ShouldNavigateEvent> = shouldNavigateChannel.receiveAsFlow()
  val dialog: StateFlow<ChooseReportTypeDialog?> = mutableDialog.asStateFlow()

  override fun onCleared() {
    job?.cancel()
  }

  fun hideDialogs() {
    mutableDialog.update { null }
  }

  fun show(dialog: ChooseReportTypeDialog) {
    mutableDialog.update { dialog }
  }

  fun createReport(type: WidgetType) {
    if (type == Custom) {
      logcat.w { "Custom reports are created by NewCustomReportViewModel" }
      return
    }

    insertWidget(type, buildEmptyMetadata(type))
  }

  private fun insertWidget(type: WidgetType, meta: JsonObject) {
    job?.cancel()
    job = viewModelScope.launch {
      val widgetId = uuidGenerator(::WidgetId)
      logcat.d { "Creating $type report $widgetId on page $page" }
      sync.addWidget(widgetId, page, type, meta)
      shouldNavigateChannel.send(ShouldNavigateEvent(widgetId))
    }
  }

  @Suppress("ElseCaseInsteadOfExhaustiveWhen")
  private fun buildEmptyMetadata(type: WidgetType): JsonObject =
    when (type) {
      // TODO: implement properly
      else -> JsonObject.Empty
    }

  data class ShouldNavigateEvent(val id: WidgetId)

  @AssistedFactory
  @ManualViewModelAssistedFactoryKey
  @ContributesIntoMap(BudgetScope::class)
  interface Factory : ManualViewModelAssistedFactory {
    fun create(page: DashboardPageId): ChooseReportTypeViewModel
  }
}
