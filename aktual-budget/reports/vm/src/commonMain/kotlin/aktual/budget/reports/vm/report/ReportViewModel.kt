package aktual.budget.reports.vm.report

import aktual.budget.db.dao.DashboardDao
import aktual.budget.model.WidgetId
import aktual.budget.model.WidgetType
import aktual.budget.reports.vm.ChartData
import aktual.budget.reports.vm.ChartDataLoader
import aktual.budget.reports.vm.CustomReportMeta
import aktual.budget.reports.vm.DashboardSync
import aktual.budget.reports.vm.FormulaReportMeta
import aktual.budget.reports.vm.MonteCarloReportMeta
import aktual.budget.reports.vm.QueryEditorData
import aktual.budget.reports.vm.QueryEditorDataLoader
import aktual.budget.reports.vm.ReportMeta
import aktual.budget.reports.vm.TextAlign
import aktual.budget.reports.vm.dashboard.DashboardItem
import aktual.budget.reports.vm.dashboard.DashboardItemDecoder
import aktual.budget.reports.vm.isSameAs
import aktual.budget.reports.vm.withDefaults
import aktual.core.Calendar
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
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
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

// Shows a report, with its settings edited as a draft that the chart previews until it's saved or
// discarded. The draft is the saved report with the user's edits applied on top, so it picks up
// renames and synced changes to the settings that weren't edited. Upstream's defaults are filled in
// first, so a report that leaves them out doesn't start with changes
@Stable
@AssistedInject
@OptIn(ExperimentalCoroutinesApi::class)
class ReportViewModel
internal constructor(
  @Assisted private val id: WidgetId,
  dashboardDao: DashboardDao,
  queryEditorDataLoader: QueryEditorDataLoader,
  private val sync: DashboardSync,
  chartDataLoader: ChartDataLoader,
  decoder: DashboardItemDecoder,
  calendar: Calendar,
  private val budgetScope: BudgetCoroutineScope,
  viewModelContext: ViewModelContext,
) : ViewModel(viewModelContext.newScope()) {
  private val saved: StateFlow<Saved> =
    dashboardDao
      .observeById(id)
      .map { widget ->
        val type = widget?.type
        val item = widget?.let(decoder::decode)
        if (type == null || item == null) {
          Saved.NotFound
        } else {
          Saved.Found(type, item, item.meta.withDefaults(calendar.today().yearMonth))
        }
      }
      .distinctUntilChanged()
      .stateIn(viewModelScope, Eagerly, Loading)

  private val edits =
    MutableStateFlow<PersistentList<(ReportMeta) -> ReportMeta>>(persistentListOf())

  // Updates straight away, so the settings show each edit while the chart catches up
  private val draft: StateFlow<ReportMeta?> =
    combine(saved, edits) { saved, edits -> (saved as? Found)?.let { edits.applyTo(it.base) } }
      .stateIn(viewModelScope, Eagerly, initialValue = null)

  private val data: StateFlow<ChartData?> =
    draft
      .flatMapLatest { draft ->
        if (draft == null || draft.ownScreen() != null) {
          flowOf(null)
        } else {
          chartDataLoader.load(draft)
        }
      }
      .stateIn(viewModelScope, Eagerly, initialValue = null)

  val state: StateFlow<ReportState> =
    combine(saved, draft, data, ::toReportState)
      .stateIn(viewModelScope, Eagerly, initialValue = Loading)

  // Null until loaded, and only loaded while something collects it
  val queryEditorData: StateFlow<QueryEditorData?> =
    queryEditorDataLoader.load().stateIn(viewModelScope, WhileSubscribed(STOP_TIMEOUT_MS), null)

  init {
    // Once the saved report has caught up with the edits, after saving or a synced change, they
    // have nothing left to do
    viewModelScope.launch {
      saved.collect { saved ->
        if (saved is Found) {
          edits.update { edits ->
            if (edits.applyTo(saved.base).isSameAs(saved.base)) persistentListOf() else edits
          }
        }
      }
    }
  }

  // Applied on top of the edits so far, so edits made in quick succession don't overwrite each
  // other
  fun edit(transform: (ReportMeta) -> ReportMeta) = edits.update { it.adding(transform) }

  fun discard() = edits.update { persistentListOf() }

  // Run in the budget's scope, so the write finishes even if the screen closes straight after
  fun save() {
    val saved = saved.value as? Found ?: return
    val draft = edits.value.applyTo(saved.base)
    if (draft.isSameAs(saved.base)) return
    logcat.d { "Saving report $id" }
    budgetScope.launch { sync.setMeta(id, saved.type, saved.item.meta, draft) }
  }

  // Read straight from the edits, since the state can lag behind an edit made just before leaving
  fun hasUnsavedChanges(): Boolean {
    val saved = saved.value as? Found ?: return false
    return !edits.value.applyTo(saved.base).isSameAs(saved.base)
  }

  fun rename(name: String) {
    logcat.d { "Renaming report $id to $name" }
    viewModelScope.launch { sync.renameWidget(id, name) }
  }

  fun saveText(content: String, align: TextAlign) {
    logcat.d { "Saving text for $id" }
    viewModelScope.launch { sync.setWidgetText(id, content, align) }
  }

  private fun toReportState(saved: Saved, draft: ReportMeta?, data: ChartData?): ReportState =
    when (saved) {
      Loading -> Loading
      NotFound -> NotFound
      is Found -> saved.item.meta.ownScreen() ?: loaded(saved, draft, data)
    }

  private fun loaded(saved: Saved.Found, draft: ReportMeta?, data: ChartData?): ReportState =
    if (draft == null || data == null) {
      Loading
    } else {
      ReportState.Loaded(
        type = saved.type,
        item = saved.item,
        data = data,
        draft = draft,
        hasChanges = !draft.isSameAs(saved.base),
      )
    }

  @Suppress("ElseCaseInsteadOfExhaustiveWhen")
  private fun ReportMeta.ownScreen(): ReportState? =
    when (this) {
      is MonteCarloReportMeta -> MonteCarlo
      is FormulaReportMeta -> Formula
      is CustomReportMeta -> Custom
      else -> null
    }

  private fun List<(ReportMeta) -> ReportMeta>.applyTo(meta: ReportMeta): ReportMeta =
    fold(meta) { acc, edit -> edit(acc) }

  private sealed interface Saved {
    data object Loading : Saved

    data object NotFound : Saved

    // The base is the saved meta with defaults filled in, which the edits apply to
    data class Found(val type: WidgetType, val item: DashboardItem, val base: ReportMeta) : Saved
  }

  private companion object {
    const val STOP_TIMEOUT_MS = 5_000L
  }

  @AssistedFactory
  @ManualViewModelAssistedFactoryKey
  @ContributesIntoMap(BudgetScope::class)
  interface Factory : ManualViewModelAssistedFactory {
    fun create(id: WidgetId): ReportViewModel
  }
}
