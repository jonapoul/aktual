package aktual.budget.reports.vm.report

import aktual.budget.db.dao.BudgetDao
import aktual.budget.db.dao.CustomReportsDao
import aktual.budget.db.dao.DashboardDao
import aktual.budget.db.dao.TagsDao
import aktual.budget.model.WidgetId
import aktual.budget.reports.vm.ChartData
import aktual.budget.reports.vm.ChartDataLoader
import aktual.budget.reports.vm.CustomReportConfig
import aktual.budget.reports.vm.CustomReportMeta
import aktual.budget.reports.vm.QueryEditorDataLoader
import aktual.budget.reports.vm.categoryGroupChoices
import aktual.budget.reports.vm.dashboard.DashboardItemDecoder
import aktual.budget.reports.vm.report.CustomReportState.Loaded
import aktual.budget.reports.vm.toConfig
import aktual.budget.tags.domain.toTagLabels
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
import kotlinx.coroutines.flow.Flow
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

// Shows a custom report, and edits its config as a draft that the chart previews. Saving the draft
// to the report isn't supported yet, so it can only be reverted
@Stable
@AssistedInject
@OptIn(ExperimentalCoroutinesApi::class)
class CustomReportViewModel
internal constructor(
  @Assisted private val id: WidgetId,
  dashboardDao: DashboardDao,
  decoder: DashboardItemDecoder,
  customReports: CustomReportsDao,
  budgetDao: BudgetDao,
  tagsDao: TagsDao,
  private val chartDataLoader: ChartDataLoader,
  queryEditorDataLoader: QueryEditorDataLoader,
) : ViewModel() {
  private val saved: StateFlow<SavedReport> =
    dashboardDao
      .observeById(id)
      .map { widget -> widget?.let(decoder::decode)?.meta as? CustomReportMeta }
      .distinctUntilChanged()
      .flatMapLatest { meta -> toSavedReport(meta, customReports) }
      .stateIn(viewModelScope, Eagerly, Loading)

  private val isEditing = MutableStateFlow(false)

  // Follows the saved report until the user changes it
  private val draft = MutableStateFlow<CustomReportConfig?>(null)

  private val preview: StateFlow<ChartData?> =
    combine(saved, draft) { saved, draft -> (saved as? Found)?.let { it to draft } }
      .distinctUntilChanged()
      .flatMapLatest(::toChartData)
      .stateIn(viewModelScope, Eagerly, initialValue = null)

  val state: StateFlow<CustomReportState> =
    combine(saved, draft, preview, isEditing, ::toCustomReportState)
      .stateIn(viewModelScope, Eagerly, Loading)

  // Null until loaded, and only loaded while the editor is open
  val editorData: StateFlow<CustomReportEditorData?> =
    combine(
        queryEditorDataLoader.load(),
        budgetDao.observeCategoryGroups(),
        budgetDao.observeCategories(),
        tagsDao.observeTags(),
      ) { query, groups, categories, tags ->
        CustomReportEditorData(
          query = query,
          categories = categoryGroupChoices(groups, categories),
          tags = tags.toTagLabels(),
        )
      }
      .stateIn(viewModelScope, WhileSubscribed(STOP_TIMEOUT_MS), null)

  init {
    viewModelScope.launch {
      var previous: CustomReportConfig? = null
      saved.collect { saved ->
        val config = (saved as? Found)?.config
        draft.update { current -> if (current == previous) config else current }
        previous = config
      }
    }
  }

  fun startEditing() = isEditing.update { draft.value != null }

  // Closing the editor keeps the draft, so the report shows it until reverted
  fun stopEditing() = isEditing.update { false }

  // Applied to the draft as it is now, so edits made in quick succession don't overwrite each other
  fun edit(transform: (CustomReportConfig) -> CustomReportConfig) {
    if (isEditing.value) draft.update { it?.let(transform) }
  }

  fun revert() {
    val config = (saved.value as? Found)?.config ?: return
    draft.update { config }
  }

  // Read straight from the draft, since the state can lag behind an edit made just before leaving
  fun hasUnsavedChanges(): Boolean {
    val saved = saved.value as? Found ?: return false
    return draft.value != saved.config
  }

  private fun toSavedReport(
    meta: CustomReportMeta?,
    customReports: CustomReportsDao,
  ): Flow<SavedReport> =
    if (meta == null) {
      flowOf(NotFound)
    } else {
      customReports.observe(meta.id).map { row ->
        if (row == null || row.tombstone == true) {
          SavedReport.NotFound
        } else {
          SavedReport.Found(meta, row.name.orEmpty(), row.toConfig())
        }
      }
    }

  private fun toChartData(pair: Pair<SavedReport.Found, CustomReportConfig?>?): Flow<ChartData?> {
    val (saved, draft) = pair ?: return flowOf(null)
    return if (draft == null) {
      chartDataLoader.custom(saved.meta)
    } else {
      chartDataLoader.custom(saved.name, draft)
    }
  }

  private fun toCustomReportState(
    saved: SavedReport,
    draft: CustomReportConfig?,
    preview: ChartData?,
    isEditing: Boolean,
  ): CustomReportState =
    when (saved) {
      Loading -> Loading
      NotFound -> NotFound
      is Found ->
        Loaded(
          name = saved.name,
          draft = draft,
          preview = preview,
          isEditing = isEditing && draft != null,
          hasChanges = draft != saved.config,
        )
    }

  @AssistedFactory
  @ManualViewModelAssistedFactoryKey
  @ContributesIntoMap(BudgetScope::class)
  interface Factory : ManualViewModelAssistedFactory {
    fun create(id: WidgetId): CustomReportViewModel
  }

  private sealed interface SavedReport {
    data object Loading : SavedReport

    data object NotFound : SavedReport

    // A null config means the row can't be read
    data class Found(
      val meta: CustomReportMeta,
      val name: String,
      val config: CustomReportConfig?,
    ) : SavedReport
  }

  private companion object {
    const val STOP_TIMEOUT_MS = 5_000L
  }
}
