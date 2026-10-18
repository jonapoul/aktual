package aktual.budget.reports.vm.report

import aktual.budget.db.DbJson
import aktual.budget.db.dao.BudgetDao
import aktual.budget.db.dao.CustomReportsDao
import aktual.budget.db.dao.TagsDao
import aktual.budget.model.CustomReportId
import aktual.budget.model.DashboardPageId
import aktual.budget.model.WidgetId
import aktual.budget.reports.vm.ChartData
import aktual.budget.reports.vm.ChartDataLoader
import aktual.budget.reports.vm.CustomReportConfig
import aktual.budget.reports.vm.CustomReportMeta
import aktual.budget.reports.vm.CustomReportSync
import aktual.budget.reports.vm.DashboardSync
import aktual.budget.reports.vm.QueryEditorDataLoader
import aktual.core.UuidGenerator
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
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.jsonObject
import logcat.logcat

// Edits a custom report that isn't saved yet, starting from upstream's defaults. Saving it names
// the report, and puts a widget for it on the page, like upstream's SaveReport.tsx
@Stable
@AssistedInject
@OptIn(ExperimentalCoroutinesApi::class)
class NewCustomReportViewModel
internal constructor(
  @Assisted private val page: DashboardPageId,
  private val uuidGenerator: UuidGenerator,
  private val reports: CustomReportSync,
  private val dashboard: DashboardSync,
  customReportsDao: CustomReportsDao,
  budgetDao: BudgetDao,
  tagsDao: TagsDao,
  chartDataLoader: ChartDataLoader,
  queryEditorDataLoader: QueryEditorDataLoader,
) : ViewModel() {
  private var job: Job? = null
  private val isEditing = MutableStateFlow(true)
  private val draft = MutableStateFlow(CustomReportConfig.Default)
  private val savedChannel = Channel<WidgetId>()

  // Emits the new report's widget once it's saved
  val savedEvent: Flow<WidgetId> = savedChannel.receiveAsFlow()

  private val preview: StateFlow<ChartData?> =
    draft
      .flatMapLatest { config -> chartDataLoader.custom(title = "", config) }
      .stateIn(viewModelScope, Eagerly, initialValue = null)

  val state: StateFlow<CustomReportState> =
    combine(draft, preview, isEditing) { draft, preview, isEditing ->
        CustomReportState.Loaded(
          name = "",
          draft = draft,
          preview = preview,
          isEditing = isEditing,
          hasChanges = draft != Default,
          isNew = true,
        )
      }
      .stateIn<CustomReportState>(viewModelScope, Eagerly, Loading)

  val editorData: StateFlow<CustomReportEditorData?> =
    customReportEditorData(queryEditorDataLoader, budgetDao, tagsDao)
      .stateIn(viewModelScope, WhileSubscribed(STOP_TIMEOUT_MS), null)

  // Names the new report can't take
  val takenNames: StateFlow<ImmutableSet<String>> =
    customReportsDao
      .observeNames()
      .map { names -> names.values.toImmutableSet() }
      .stateIn(viewModelScope, Eagerly, persistentSetOf())

  override fun onCleared() {
    job?.cancel()
  }

  fun startEditing() = isEditing.update { true }

  fun stopEditing() = isEditing.update { false }

  fun edit(transform: (CustomReportConfig) -> CustomReportConfig) {
    if (isEditing.value) draft.update(transform)
  }

  fun revert() = draft.update { Default }

  fun hasUnsavedChanges(): Boolean = draft.value != Default

  fun save(name: String) {
    if (job?.isActive == true) return
    val config = draft.value
    job = viewModelScope.launch {
      val error = reports.validateName(name)
      if (error != null) {
        logcat.w { "Can't save custom report as '$name': $error" }
        return@launch
      }

      val reportId = uuidGenerator(::CustomReportId)
      val widgetId = uuidGenerator(::WidgetId)
      logcat.d { "Creating custom report $reportId with widget $widgetId on page $page" }
      reports.insert(reportId, name, config)
      val meta =
        DbJson.encodeToJsonElement(CustomReportMeta.serializer(), CustomReportMeta(reportId))
      dashboard.addWidget(widgetId, page, Custom, meta.jsonObject)
      savedChannel.send(widgetId)
    }
  }

  @AssistedFactory
  @ManualViewModelAssistedFactoryKey
  @ContributesIntoMap(BudgetScope::class)
  interface Factory : ManualViewModelAssistedFactory {
    fun create(page: DashboardPageId): NewCustomReportViewModel
  }

  private companion object {
    const val STOP_TIMEOUT_MS = 5_000L
  }
}
