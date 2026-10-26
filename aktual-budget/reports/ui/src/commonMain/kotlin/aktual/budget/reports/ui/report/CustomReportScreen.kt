package aktual.budget.reports.ui.report

import aktual.budget.model.WidgetId
import aktual.budget.model.WidgetType
import aktual.budget.reports.ui.charts.ChartInfoSheet
import aktual.budget.reports.ui.charts.ChartZoomState
import aktual.budget.reports.ui.charts.PREVIEW_CUSTOM_DATA
import aktual.budget.reports.ui.charts.info
import aktual.budget.reports.ui.charts.rememberChartZoomState
import aktual.budget.reports.ui.string
import aktual.budget.reports.vm.report.CustomReportEditorData
import aktual.budget.reports.vm.report.CustomReportState
import aktual.budget.reports.vm.report.CustomReportViewModel
import aktual.core.l10n.Strings
import aktual.core.nav.BackNavigator
import aktual.core.nav.TransactionsNavigator
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.BackHandler
import aktual.core.ui.ColorParams
import aktual.core.ui.ColorParamsProvider
import aktual.core.ui.PageBackground
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColorParams
import androidx.compose.foundation.layout.Box
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

// Shows a custom report, and edits its config with the chart previewing each change. Changes stay
// after the editor closes, until reverted or the screen is left
@Composable
internal fun CustomReportScreen(
  id: WidgetId,
  back: BackNavigator,
  toTransactions: TransactionsNavigator,
  viewModel: CustomReportViewModel = customReportViewModel(id),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  val editorData by viewModel.editorData.collectAsStateWithLifecycle()
  var showDiscard by rememberSaveable { mutableStateOf(false) }
  val loaded = state as? Loaded
  val isEditing = loaded?.isEditing == true

  val leave = { if (viewModel.hasUnsavedChanges()) showDiscard = true else back() }
  val drillDown = rememberDrilldown(loaded?.preview, toTransactions)

  BackHandler(enabled = isEditing || loaded?.hasChanges == true) {
    if (isEditing) viewModel.stopEditing() else leave()
  }

  CustomReportScaffold(
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
        is DrillDown -> drillDown(action.drilldown)
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
private fun customReportViewModel(id: WidgetId) =
  assistedMetroViewModel<CustomReportViewModel, CustomReportViewModel.Factory>(key = id.value) {
    create(id)
  }

@Composable
internal fun CustomReportScaffold(
  state: CustomReportState,
  onAction: (CustomReportAction) -> Unit,
  modifier: Modifier = Modifier,
  editorData: CustomReportEditorData? = null,
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
      val name = loaded?.name?.takeUnless { it.isBlank() }
      ReportEditorTopBar(
        title =
          when {
            name != null -> name
            loaded?.isNew == true -> Strings.reportsCustomEditorNew
            else -> WidgetType.Custom.string()
          },
        isEditing = loaded?.isEditing == true,
        hasChanges = loaded?.hasChanges == true,
        canSave = loaded?.draft != null && (loaded.hasChanges || loaded.isNew),
        canEdit = loaded?.draft != null,
        preview = preview,
        zoom = zoom,
        onShowInfo = { showInfo = true },
        onAction = onAction,
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
          CustomReportContent(
            modifier = contentModifier,
            state = state,
            editorData = editorData,
            zoom = zoom,
            onAction = onAction,
          )
        }
      }
    }
  }
}

@Composable
private fun CustomReportContent(
  state: CustomReportState.Loaded,
  editorData: CustomReportEditorData?,
  zoom: ChartZoomState,
  onAction: (CustomReportAction) -> Unit,
  modifier: Modifier = Modifier,
) {
  val draft = state.draft
  val isEditing = state.isEditing && draft != null
  ReportEditorContent(
    modifier = modifier,
    preview = state.preview,
    isEditing = isEditing,
    zoom = zoom,
    onChartAction = { action ->
      if (action is DrillDown) onAction(CustomReportAction.DrillDown(action.drilldown))
    },
  ) {
    if (isEditing) {
      CustomReportSettings(
        modifier = Modifier.editorSettingsModifier(),
        draft = draft,
        preview = state.preview,
        editorData = editorData,
        onAction = onAction,
      )
    } else if (draft == null) {
      Text(
        modifier = Modifier.padding(8.dp),
        text = Strings.reportsCustomEditorUnreadable,
        style = typography.bodyMedium,
        color = colors.pageTextSubdued,
      )
    }
  }
}

@PortraitPreview
@Composable
private fun PreviewCustomReportScaffold(
  @PreviewParameter(CustomReportProvider::class) params: ColorParams<CustomReportState>,
) = PreviewWithColorParams(params) { CustomReportScaffold(state = this, onAction = {}) }

private class CustomReportProvider :
  ColorParamsProvider<CustomReportState>(
    CustomReportState.Loaded(
      name = PREVIEW_CUSTOM_DATA.title,
      draft = PREVIEW_CUSTOM_DATA.config,
      preview = PREVIEW_CUSTOM_DATA,
      isEditing = false,
      hasChanges = false,
    ),
    CustomReportState.Loaded(
      name = PREVIEW_CUSTOM_DATA.title,
      draft = PREVIEW_CUSTOM_DATA.config,
      preview = PREVIEW_CUSTOM_DATA,
      isEditing = true,
      hasChanges = true,
    ),
    Loading,
    NotFound,
  )
