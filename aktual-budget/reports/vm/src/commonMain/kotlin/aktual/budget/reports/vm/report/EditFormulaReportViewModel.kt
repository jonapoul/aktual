package aktual.budget.reports.vm.report

import aktual.budget.db.dao.DashboardDao
import aktual.budget.model.WidgetId
import aktual.budget.reports.vm.ChartDataLoader
import aktual.budget.reports.vm.DashboardSync
import aktual.budget.reports.vm.FormulaData
import aktual.budget.reports.vm.FormulaEdits
import aktual.budget.reports.vm.FormulaReportMeta
import aktual.budget.reports.vm.dashboard.DashboardItemDecoder
import aktual.budget.reports.vm.toEdits
import aktual.budget.reports.vm.withEdits
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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import logcat.logcat

// Holds a formula card's settings as a draft, which only reaches the card when saved
@Stable
@AssistedInject
@OptIn(ExperimentalCoroutinesApi::class)
class EditFormulaReportViewModel
internal constructor(
  @Assisted private val id: WidgetId,
  dashboardDao: DashboardDao,
  decoder: DashboardItemDecoder,
  chartDataLoader: ChartDataLoader,
  private val sync: DashboardSync,
  private val budgetScope: BudgetCoroutineScope,
) : ViewModel() {
  private val saved: StateFlow<SavedMeta> =
    dashboardDao
      .observeById(id)
      .map { widget ->
        val meta = widget?.let(decoder::decode)?.meta
        if (meta is FormulaReportMeta) SavedMeta.Found(meta) else SavedMeta.NotFound
      }
      .stateIn(viewModelScope, Eagerly, Loading)

  // Taken from the saved meta once, and only changed by the user from then on
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

  val state: StateFlow<EditFormulaReportState> =
    combine(saved, draft, preview) { saved, draft, preview ->
        when (saved) {
          Loading -> EditFormulaReportState.Loading
          NotFound -> EditFormulaReportState.NotFound
          is Found ->
            if (draft == null) {
              EditFormulaReportState.Loading
            } else {
              EditFormulaReportState.Loaded(
                draft = draft,
                preview = preview,
                hasChanges = draft != saved.meta.toEdits(),
              )
            }
        }
      }
      .stateIn(viewModelScope, Eagerly, Loading)

  init {
    viewModelScope.launch {
      val found = saved.first { it != Loading } as? SavedMeta.Found ?: return@launch
      draft.update { found.meta.toEdits() }
    }
  }

  // Applied to the draft as it is now, so edits made in quick succession don't overwrite each other
  fun edit(transform: (FormulaEdits) -> FormulaEdits) = draft.update { it?.let(transform) }

  // Run in the budget's scope, so the write finishes after the editor closes
  fun save() {
    val edits = draft.value ?: return
    logcat.d { "Saving formula for $id" }
    budgetScope.launch { sync.setFormula(id, edits) }
  }

  // Read straight from the draft, since the state can lag behind an edit made just before leaving
  fun hasUnsavedChanges(): Boolean {
    val meta = (saved.value as? SavedMeta.Found)?.meta ?: return false
    val edits = draft.value ?: return false
    return edits != meta.toEdits()
  }

  @AssistedFactory
  @ManualViewModelAssistedFactoryKey
  @ContributesIntoMap(BudgetScope::class)
  interface Factory : ManualViewModelAssistedFactory {
    fun create(id: WidgetId): EditFormulaReportViewModel
  }

  private sealed interface SavedMeta {
    data object Loading : SavedMeta

    data object NotFound : SavedMeta

    data class Found(val meta: FormulaReportMeta) : SavedMeta
  }
}
