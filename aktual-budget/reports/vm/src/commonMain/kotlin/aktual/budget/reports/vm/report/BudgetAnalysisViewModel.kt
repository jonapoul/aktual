package aktual.budget.reports.vm.report

import aktual.budget.db.dao.DashboardDao
import aktual.budget.model.WidgetId
import aktual.budget.reports.vm.BudgetAnalysisData
import aktual.budget.reports.vm.BudgetAnalysisReportMeta
import aktual.budget.reports.vm.BudgetAnalysisSettings
import aktual.budget.reports.vm.ChartDataLoader
import aktual.budget.reports.vm.DashboardSync
import aktual.budget.reports.vm.QueryEditorData
import aktual.budget.reports.vm.QueryEditorDataLoader
import aktual.budget.reports.vm.dashboard.DashboardItemDecoder
import aktual.budget.reports.vm.report.BudgetAnalysisState.Loaded
import aktual.budget.reports.vm.toSettings
import aktual.budget.reports.vm.withSettings
import aktual.core.Calendar
import aktual.di.BudgetCoroutineScope
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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.yearMonth
import logcat.logcat

// Shows a budget analysis report, and edits its settings as a draft that the chart previews until
// it's saved or reverted
@Stable
@AssistedInject
@OptIn(ExperimentalCoroutinesApi::class)
class BudgetAnalysisViewModel
internal constructor(
  @Assisted private val id: WidgetId,
  dashboardDao: DashboardDao,
  decoder: DashboardItemDecoder,
  chartDataLoader: ChartDataLoader,
  queryEditorDataLoader: QueryEditorDataLoader,
  private val sync: DashboardSync,
  private val calendar: Calendar,
  private val budgetScope: BudgetCoroutineScope,
) : ViewModel() {
  private val saved: StateFlow<SavedMeta> =
    dashboardDao
      .observeById(id)
      .map { widget ->
        val meta = widget?.let(decoder::decode)?.meta
        if (meta is BudgetAnalysisReportMeta) SavedMeta.Found(meta) else SavedMeta.NotFound
      }
      .distinctUntilChanged()
      .stateIn(viewModelScope, Eagerly, Loading)

  private val isEditing = MutableStateFlow(false)

  // Follows the saved report until the user changes it
  private val draft = MutableStateFlow<BudgetAnalysisSettings?>(null)

  private val preview: StateFlow<BudgetAnalysisData?> =
    combine(saved, draft) { saved, draft ->
        if (saved is Found && draft != null) saved.meta.withSettings(draft) else null
      }
      .distinctUntilChanged()
      .flatMapLatest { meta ->
        if (meta == null) {
          flowOf(null)
        } else {
          chartDataLoader.budgetAnalysis(meta).map { it as? BudgetAnalysisData }
        }
      }
      .stateIn(viewModelScope, Eagerly, initialValue = null)

  val state: StateFlow<BudgetAnalysisState> =
    combine(saved, draft, preview, isEditing, ::toState).stateIn(viewModelScope, Eagerly, Loading)

  // Null until loaded, and only loaded while the editor is open
  val queryEditorData: StateFlow<QueryEditorData?> =
    queryEditorDataLoader.load().stateIn(viewModelScope, WhileSubscribed(STOP_TIMEOUT_MS), null)

  init {
    viewModelScope.launch {
      var previous: BudgetAnalysisSettings? = null
      saved.collect { saved ->
        val settings = (saved as? Found)?.settings()
        draft.update { current -> if (current == previous) settings else current }
        previous = settings
      }
    }
  }

  fun startEditing() = isEditing.update { draft.value != null }

  // Closing the editor keeps the draft, so the report shows it until reverted
  fun stopEditing() = isEditing.update { false }

  // Applied to the draft as it is now, so edits made in quick succession don't overwrite each other
  fun edit(transform: (BudgetAnalysisSettings) -> BudgetAnalysisSettings) {
    if (isEditing.value) draft.update { it?.let(transform) }
  }

  fun revert() {
    val settings = (saved.value as? Found)?.settings() ?: return
    draft.update { settings }
  }

  // Run in the budget's scope, so the write finishes even if the screen closes straight after. The
  // saved report then follows the draft
  fun save() {
    val draft = draft.value ?: return
    if (!hasUnsavedChanges()) return
    isEditing.update { false }
    logcat.d { "Saving budget analysis $id" }
    budgetScope.launch { sync.setBudgetAnalysis(id, draft) }
  }

  // Read straight from the draft, since the state can lag behind an edit made just before leaving
  fun hasUnsavedChanges(): Boolean {
    val saved = saved.value as? Found ?: return false
    return draft.value != saved.settings()
  }

  private fun SavedMeta.Found.settings(): BudgetAnalysisSettings =
    meta.toSettings(calendar.today().yearMonth)

  private fun toState(
    saved: SavedMeta,
    draft: BudgetAnalysisSettings?,
    preview: BudgetAnalysisData?,
    isEditing: Boolean,
  ): BudgetAnalysisState =
    when (saved) {
      Loading -> Loading
      NotFound -> NotFound
      is Found ->
        if (draft == null) {
          Loading
        } else {
          Loaded(
            name = saved.meta.name,
            draft = draft,
            preview = preview,
            isEditing = isEditing,
            hasChanges = draft != saved.settings(),
          )
        }
    }

  @AssistedFactory
  @ManualViewModelAssistedFactoryKey
  @ContributesIntoMap(BudgetScope::class)
  interface Factory : ManualViewModelAssistedFactory {
    fun create(id: WidgetId): BudgetAnalysisViewModel
  }

  private sealed interface SavedMeta {
    data object Loading : SavedMeta

    data object NotFound : SavedMeta

    data class Found(val meta: BudgetAnalysisReportMeta) : SavedMeta
  }

  private companion object {
    const val STOP_TIMEOUT_MS = 5_000L
  }
}
