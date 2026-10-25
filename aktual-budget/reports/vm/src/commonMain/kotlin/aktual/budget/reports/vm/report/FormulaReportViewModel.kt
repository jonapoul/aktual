package aktual.budget.reports.vm.report

import aktual.budget.db.dao.DashboardDao
import aktual.budget.model.WidgetId
import aktual.budget.reports.vm.ChartDataLoader
import aktual.budget.reports.vm.DashboardSync
import aktual.budget.reports.vm.FormulaData
import aktual.budget.reports.vm.FormulaEdits
import aktual.budget.reports.vm.FormulaReportMeta
import aktual.budget.reports.vm.QueryEditorData
import aktual.budget.reports.vm.QueryEditorDataLoader
import aktual.budget.reports.vm.dashboard.DashboardItemDecoder
import aktual.budget.reports.vm.toEdits
import aktual.budget.reports.vm.withEdits
import aktual.di.BudgetCoroutineScope
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import logcat.logcat

// Shows a formula card, and edits its settings as a draft which only reaches the card when saved
@Stable
@AssistedInject
@OptIn(ExperimentalCoroutinesApi::class)
class FormulaReportViewModel
internal constructor(
  @Assisted private val id: WidgetId,
  dashboardDao: DashboardDao,
  decoder: DashboardItemDecoder,
  chartDataLoader: ChartDataLoader,
  queryEditorDataLoader: QueryEditorDataLoader,
  private val sync: DashboardSync,
  private val budgetScope: BudgetCoroutineScope,
  viewModelContext: ViewModelContext,
) : ViewModel(viewModelContext.newScope()) {
  private val saved: StateFlow<SavedMeta> =
    dashboardDao
      .observeById(id)
      .map { widget ->
        val meta = widget?.let(decoder::decode)?.meta
        if (meta is FormulaReportMeta) SavedMeta.Found(meta) else SavedMeta.NotFound
      }
      .stateIn(viewModelScope, Eagerly, Loading)

  private val isEditing = MutableStateFlow(false)

  // Follows the saved card while viewing, and only changes with the user's edits while editing
  private val draft = MutableStateFlow<FormulaEdits?>(null)

  private val preview: StateFlow<FormulaData?> =
    combine(saved, draft) { saved, draft ->
        if (saved is Found && draft != null) saved.meta.withEdits(draft) else null
      }
      .distinctUntilChanged()
      .flatMapLatest { meta ->
        if (meta == null) flowOf(null) else chartDataLoader.formula(meta).map { it as? FormulaData }
      }
      .stateIn(viewModelScope, Eagerly, initialValue = null)

  val state: StateFlow<FormulaReportState> =
    combine(saved, draft, preview, isEditing) { saved, draft, preview, isEditing ->
        when (saved) {
          Loading -> FormulaReportState.Loading
          NotFound -> FormulaReportState.NotFound
          is Found ->
            if (draft == null) {
              FormulaReportState.Loading
            } else {
              FormulaReportState.Loaded(
                draft = draft,
                preview = preview,
                isEditing = isEditing,
                hasChanges = isEditing && draft != saved.meta.toEdits(),
              )
            }
        }
      }
      .stateIn(viewModelScope, Eagerly, Loading)

  // Null until loaded, and only loaded while something shows query conditions
  val queryEditorData: StateFlow<QueryEditorData?> =
    queryEditorDataLoader
      .load()
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), null)

  init {
    viewModelScope.launch {
      saved.collect { saved ->
        if (saved is Found && !isEditing.value) draft.update { saved.meta.toEdits() }
      }
    }
  }

  fun startEditing() = isEditing.update { true }

  // Applied to the draft as it is now, so edits made in quick succession don't overwrite each other
  fun edit(transform: (FormulaEdits) -> FormulaEdits) {
    if (isEditing.value) draft.update { it?.let(transform) }
  }

  // Run in the budget's scope, so the write finishes even if the screen closes straight after. The
  // draft keeps the saved values until the card's update comes back
  fun save() {
    val edits = draft.value ?: return
    logcat.d { "Saving formula for $id" }
    isEditing.update { false }
    budgetScope.launch { sync.setFormula(id, edits) }
  }

  fun discard() {
    isEditing.update { false }
    val meta = (saved.value as? Found)?.meta ?: return
    draft.update { meta.toEdits() }
  }

  // Read straight from the draft, since the state can lag behind an edit made just before leaving
  fun hasUnsavedChanges(): Boolean {
    if (!isEditing.value) return false
    val meta = (saved.value as? Found)?.meta ?: return false
    val edits = draft.value ?: return false
    return edits != meta.toEdits()
  }

  @AssistedFactory
  @ManualViewModelAssistedFactoryKey
  @ContributesIntoMap(BudgetScope::class)
  interface Factory : ManualViewModelAssistedFactory {
    fun create(id: WidgetId): FormulaReportViewModel
  }

  private sealed interface SavedMeta {
    data object Loading : SavedMeta

    data object NotFound : SavedMeta

    data class Found(val meta: FormulaReportMeta) : SavedMeta
  }

  private companion object {
    const val STOP_TIMEOUT_MS = 5_000L
  }
}
