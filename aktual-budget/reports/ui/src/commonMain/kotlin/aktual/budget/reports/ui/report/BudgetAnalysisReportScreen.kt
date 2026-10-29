package aktual.budget.reports.ui.report

import aktual.budget.model.WidgetId
import aktual.budget.model.WidgetType
import aktual.budget.reports.ui.charts.ChartInfoSheet
import aktual.budget.reports.ui.charts.PREVIEW_BUDGET_ANALYSIS_DATA
import aktual.budget.reports.ui.charts.info
import aktual.budget.reports.ui.charts.rememberChartZoomState
import aktual.budget.reports.ui.string
import aktual.budget.reports.vm.BUDGET_ANALYSIS_FIELDS
import aktual.budget.reports.vm.BudgetAnalysisSettings
import aktual.budget.reports.vm.GraphType
import aktual.budget.reports.vm.QueryEditorData
import aktual.budget.reports.vm.TimeFrame
import aktual.budget.reports.vm.report.BudgetAnalysisState
import aktual.budget.reports.vm.report.BudgetAnalysisViewModel
import aktual.core.l10n.Strings
import aktual.core.nav.BackNavigator
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.BackHandler
import aktual.core.ui.ColorParams
import aktual.core.ui.ColorParamsProvider
import aktual.core.ui.PageBackground
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColorParams
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.YearMonth

// packages/desktop-client/src/components/reports/reports/BudgetAnalysis.tsx. Changes stay after the
// editor closes, until saved, reverted or the screen is left
@Composable
internal fun BudgetAnalysisReportScreen(
  id: WidgetId,
  back: BackNavigator,
  viewModel: BudgetAnalysisViewModel = budgetAnalysisViewModel(id),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  val editorData by viewModel.queryEditorData.collectAsStateWithLifecycle()
  var showDiscard by rememberSaveable { mutableStateOf(false) }
  val loaded = state as? Loaded
  val isEditing = loaded?.isEditing == true

  val leave = { if (viewModel.hasUnsavedChanges()) showDiscard = true else back() }

  BackHandler(enabled = isEditing || loaded?.hasChanges == true) {
    if (isEditing) viewModel.stopEditing() else leave()
  }

  BudgetAnalysisScaffold(
    state = state,
    editorData = editorData,
    onAction = { action ->
      when (action) {
        ReportEditorAction.Back -> leave()
        ReportEditorAction.StartEditing -> viewModel.startEditing()
        ReportEditorAction.StopEditing -> viewModel.stopEditing()
        ReportEditorAction.Revert -> viewModel.revert()
        ReportEditorAction.Save -> viewModel.save()
        is Edit -> viewModel.edit(action.transform)
        is Rename -> viewModel.rename(action.name)
      }
    },
  )

  if (showDiscard) {
    DiscardChangesDialog(
      message = Strings.reportsCustomEditorDiscardMessage,
      onDiscard = {
        showDiscard = false
        back()
      },
      onCancel = { showDiscard = false },
    )
  }
}

@Composable
private fun budgetAnalysisViewModel(id: WidgetId) =
  assistedMetroViewModel<BudgetAnalysisViewModel, BudgetAnalysisViewModel.Factory>(
    key = id.value,
  ) {
    create(id)
  }

@Composable
internal fun BudgetAnalysisScaffold(
  state: BudgetAnalysisState,
  onAction: (BudgetAnalysisAction) -> Unit,
  modifier: Modifier = Modifier,
  editorData: QueryEditorData? = null,
) {
  val loaded = state as? Loaded
  val preview = loaded?.preview
  val zoom = rememberChartZoomState(preview)
  var showInfo by remember { mutableStateOf(false) }

  if (showInfo && preview != null) {
    ChartInfoSheet(info = preview.info(), onDismiss = { showInfo = false })
  }

  Scaffold(
    modifier = modifier.fillMaxSize(),
    topBar = {
      ReportEditorTopBar(
        title = loaded?.name?.takeUnless { it.isBlank() } ?: WidgetType.BudgetAnalysis.string(),
        isEditing = loaded?.isEditing == true,
        hasChanges = loaded?.hasChanges == true,
        canSave = loaded?.hasChanges == true,
        canEdit = loaded != null,
        preview = preview,
        zoom = zoom,
        onShowInfo = { showInfo = true },
        onAction = onAction,
        menu = {
          if (loaded != null) {
            ReportMenu(
              name = loaded.name.orEmpty(),
              onRename = { onAction(BudgetAnalysisAction.Rename(it)) },
            )
          }
        },
      )
    },
  ) { innerPadding ->
    Box(modifier = Modifier.fillMaxSize()) {
      PageBackground()
      val contentModifier = Modifier.padding(innerPadding).fillMaxSize()
      when (state) {
        Loading -> {
          Box(modifier = contentModifier, contentAlignment = Center) {
            CircularProgressIndicator(color = colors.pageText)
          }
        }
        NotFound -> {
          Box(modifier = contentModifier, contentAlignment = Center) {
            Text(text = Strings.reportsNotFound, color = colors.pageText)
          }
        }
        is Loaded -> {
          ReportEditorContent(
            modifier = contentModifier,
            preview = preview,
            isEditing = state.isEditing,
            zoom = zoom,
          ) {
            if (state.isEditing) {
              BudgetAnalysisSettings(
                modifier = Modifier.editorSettingsModifier(),
                draft = state.draft,
                editorData = editorData,
                onAction = onAction,
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun BudgetAnalysisSettings(
  draft: BudgetAnalysisSettings,
  editorData: QueryEditorData?,
  onAction: (BudgetAnalysisAction) -> Unit,
  modifier: Modifier = Modifier,
) {
  var sheet by rememberSaveable { mutableStateOf<BudgetAnalysisSheet?>(null) }
  val edit = { transform: (BudgetAnalysisSettings) -> BudgetAnalysisSettings ->
    onAction(BudgetAnalysisAction.Edit(transform))
  }

  Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(20.dp)) {
    Column {
      SectionLabel(Strings.reportsFormulaQueryTimeFrame)
      TimeFrameRow(timeFrame = draft.timeFrame, onClick = { sheet = Dates })
    }

    Section(label = Strings.reportsCustomEditorDisplay) {
      SettingValueRow(Strings.reportsCustomEditorGraph, draft.graphType.label()) { sheet = Graph }
      Divider()
      SwitchRow(Strings.reportsBudgetAnalysisShowBalance, draft.showBalance) { show ->
        edit { it.copy(showBalance = show) }
      }
      Divider()
      SwitchRow(Strings.reportsBudgetAnalysisBalanceOnly, draft.balanceOnly) { only ->
        edit { it.copy(balanceOnly = only) }
      }
      Divider()
      SwitchRow(Strings.reportsCustomEditorShowHidden, draft.showHiddenCategories) { show ->
        edit { it.copy(showHiddenCategories = show) }
      }
    }

    if (editorData != null) {
      Column {
        SectionLabel(Strings.reportsCustomEditorFilters)
        ConditionsEditor(
          conditions = draft.conditions,
          conditionsOp = draft.conditionsOp,
          data = editorData,
          fields = BUDGET_ANALYSIS_FIELDS,
          onChange = { conditions, op ->
            edit { it.copy(conditions = conditions.toImmutableList(), conditionsOp = op) }
          },
        )
      }
    }
  }

  when (sheet) {
    Dates -> {
      if (editorData != null) {
        TimeFrameSheet(
          timeFrame = draft.timeFrame,
          data = editorData,
          onChange = { timeFrame -> edit { it.copy(timeFrame = timeFrame) } },
          onDismiss = { sheet = null },
        )
      }
    }
    Graph -> {
      OptionSheet(
        title = Strings.reportsCustomEditorGraph,
        options = GRAPH_TYPES,
        selected = draft.graphType,
        isEnabled = { true },
        label = { it.label() },
        onSelect = { graph -> edit { it.copy(graphType = graph) } },
        onDismiss = { sheet = null },
      )
    }
    null -> {}
  }
}

private enum class BudgetAnalysisSheet {
  Dates,
  Graph,
}

@Composable
private fun GraphType.label(): String =
  when (this) {
    Bar -> Strings.reportsCustomEditorGraphBar
    Line -> Strings.reportsCustomEditorGraphLine
    Unknown -> Strings.reportsCustomEditorUnknown
  }

private val GRAPH_TYPES = persistentListOf<GraphType>(Bar, Line)

@PortraitPreview
@Composable
private fun PreviewBudgetAnalysisScaffold(
  @PreviewParameter(BudgetAnalysisProvider::class) params: ColorParams<BudgetAnalysisState>,
) = PreviewWithColorParams(params) { BudgetAnalysisScaffold(state = this, onAction = {}) }

private val PREVIEW_SETTINGS =
  BudgetAnalysisSettings(
    timeFrame = TimeFrame(YearMonth(2025, 1), YearMonth(2025, 6), Static),
    conditions = persistentListOf(),
    conditionsOp = And,
    graphType = Bar,
    showBalance = true,
    balanceOnly = false,
    showHiddenCategories = false,
  )

private class BudgetAnalysisProvider :
  ColorParamsProvider<BudgetAnalysisState>(
    BudgetAnalysisState.Loaded(
      name = null,
      draft = PREVIEW_SETTINGS,
      preview = PREVIEW_BUDGET_ANALYSIS_DATA,
      isEditing = false,
      hasChanges = false,
    ),
    BudgetAnalysisState.Loaded(
      name = "Groceries",
      draft = PREVIEW_SETTINGS,
      preview = PREVIEW_BUDGET_ANALYSIS_DATA,
      isEditing = true,
      hasChanges = true,
    ),
    BudgetAnalysisState.Loading,
    BudgetAnalysisState.NotFound,
  )
