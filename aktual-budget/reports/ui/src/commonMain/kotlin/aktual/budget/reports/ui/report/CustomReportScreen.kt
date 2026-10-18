package aktual.budget.reports.ui.report

import aktual.budget.model.WidgetId
import aktual.budget.model.WidgetType
import aktual.budget.reports.ui.Tags
import aktual.budget.reports.ui.charts.ChartInfoSheet
import aktual.budget.reports.ui.charts.ChartZoomState
import aktual.budget.reports.ui.charts.PREVIEW_CUSTOM_DATA
import aktual.budget.reports.ui.charts.ReportChart
import aktual.budget.reports.ui.charts.info
import aktual.budget.reports.ui.charts.rememberChartZoomState
import aktual.budget.reports.ui.string
import aktual.budget.reports.vm.ChartData
import aktual.budget.reports.vm.report.CustomReportEditorData
import aktual.budget.reports.vm.report.CustomReportState
import aktual.budget.reports.vm.report.CustomReportViewModel
import aktual.core.icons.material.Clear
import aktual.core.icons.material.Edit
import aktual.core.icons.material.Info
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.ZoomOut
import aktual.core.l10n.Strings
import aktual.core.nav.BackNavigator
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.BackHandler
import aktual.core.ui.BareIconButton
import aktual.core.ui.BottomSpacing
import aktual.core.ui.CardShape
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParams
import aktual.core.ui.NavBackIconButton
import aktual.core.ui.PageBackground
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColoredParams
import aktual.core.ui.PrimaryIconButton
import aktual.core.ui.PrimaryTextButton
import aktual.core.ui.transparentTopAppBarColors
import aktual.core.ui.verticalScrollWithBar
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel

// Shows a custom report, and edits its config with the chart previewing each change. Changes stay
// after the editor closes, until reverted or the screen is left
@Composable
internal fun CustomReportScreen(
  id: WidgetId,
  back: BackNavigator,
  viewModel: CustomReportViewModel = customReportViewModel(id),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  val editorData by viewModel.editorData.collectAsStateWithLifecycle()
  var showDiscard by rememberSaveable { mutableStateOf(false) }
  val loaded = state as? CustomReportState.Loaded
  val isEditing = loaded?.isEditing == true

  val leave = { if (viewModel.hasUnsavedChanges()) showDiscard = true else back() }

  BackHandler(enabled = isEditing || loaded?.hasChanges == true) {
    if (isEditing) viewModel.stopEditing() else leave()
  }

  CustomReportScaffold(
    state = state,
    editorData = editorData,
    onAction = { action ->
      when (action) {
        Back -> leave()
        StartEditing -> viewModel.startEditing()
        StopEditing -> viewModel.stopEditing()
        Revert -> viewModel.revert()
        Save -> viewModel.save()
        is Edit -> viewModel.edit(action.transform)
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
      CustomReportTopBar(
        loaded = loaded,
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
private fun CustomReportTopBar(
  loaded: CustomReportState.Loaded?,
  zoom: ChartZoomState,
  onShowInfo: () -> Unit,
  onAction: (CustomReportAction) -> Unit,
) {
  val isEditing = loaded?.isEditing == true
  TopAppBar(
    colors = colors.transparentTopAppBarColors(),
    navigationIcon = {
      if (isEditing) {
        IconButton(
          modifier = Modifier.testTag(Tags.CloseEditor),
          onClick = { onAction(StopEditing) },
        ) {
          Icon(
            imageVector = MaterialIcons.Clear,
            contentDescription = Strings.reportsFormulaEditorClose,
          )
        }
      } else {
        NavBackIconButton(onClick = { onAction(Back) })
      }
    },
    title = {
      Column {
        val name = loaded?.name?.takeUnless { it.isBlank() }
        Text(
          text =
            when {
              isEditing -> Strings.reportsFormulaViewerEdit
              name != null -> name
              loaded?.isNew == true -> Strings.reportsCustomEditorNew
              else -> WidgetType.Custom.string()
            },
        )
        AnimatedVisibility(visible = loaded?.hasChanges == true) {
          Text(
            text = Strings.reportsCustomEditorUnsaved,
            style = typography.labelMedium,
            color = colors.warningText,
          )
        }
      }
    },
    actions = {
      if (loaded?.hasChanges == true) {
        TextButton(modifier = Modifier.testTag(Tags.RevertReport), onClick = { onAction(Revert) }) {
          Text(Strings.reportsCustomEditorRevert)
        }
      }
      if (loaded?.draft != null && (loaded.hasChanges || loaded.isNew)) {
        PrimaryTextButton(
          modifier = Modifier.padding(end = 8.dp).testTag(Tags.SaveReport),
          text = Strings.reportsFormulaEditorSave,
          shape = CircleShape,
          onClick = { onAction(Save) },
        )
      }
      if (!isEditing && zoom.range != null) {
        BareIconButton(
          modifier = Modifier.testTag(Tags.ResetZoom),
          imageVector = MaterialIcons.ZoomOut,
          contentDescription = Strings.reportsResetZoom,
          onClick = zoom::reset,
        )
      }
      if (!isEditing && loaded?.preview?.info()?.isNotEmpty() == true) {
        BareIconButton(
          modifier = Modifier.testTag(Tags.ChartInfo),
          imageVector = MaterialIcons.Info,
          contentDescription = Strings.reportsChartInfo,
          onClick = onShowInfo,
        )
      }
      if (!isEditing && loaded?.draft != null) {
        PrimaryIconButton(
          modifier = Modifier.padding(horizontal = 8.dp).testTag(Tags.EditReport),
          imageVector = MaterialIcons.Edit,
          contentDescription = Strings.reportsFormulaViewerEdit,
          size = 20.dp,
          shape = CircleShape,
          onClick = { onAction(StartEditing) },
        )
      }
    },
  )
}

// While editing, the chart shrinks to a preview above the settings, which scroll beneath it
@Composable
private fun CustomReportContent(
  state: CustomReportState.Loaded,
  editorData: CustomReportEditorData?,
  zoom: ChartZoomState,
  onAction: (CustomReportAction) -> Unit,
  modifier: Modifier = Modifier,
) =
  Column(modifier = modifier.padding(CONTENT_PADDING)) {
    val draft = state.draft
    val isEditing = state.isEditing && draft != null
    ChartCard(
      modifier =
        if (isEditing) {
          Modifier.fillMaxWidth().height(PREVIEW_HEIGHT)
        } else {
          Modifier.fillMaxWidth().weight(1f)
        },
      preview = state.preview,
      isEditing = isEditing,
      zoom = zoom,
    )

    if (isEditing) {
      CustomReportSettings(
        modifier =
          Modifier.fillMaxWidth()
            .weight(1f)
            .verticalScrollWithBar(rememberScrollState())
            .padding(top = 20.dp),
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
    BottomSpacing()
  }

@Composable
private fun ChartCard(
  preview: ChartData?,
  isEditing: Boolean,
  zoom: ChartZoomState,
  modifier: Modifier = Modifier,
) =
  Box(modifier = modifier.background(colors.tableBackground, CardShape).padding(8.dp)) {
    if (preview == null) {
      CircularProgressIndicator(modifier = Modifier.align(Center), color = colors.pageText)
    } else {
      ReportChart(
        modifier = Modifier.fillMaxSize(),
        data = preview,
        compact = isEditing,
        onAction = {},
        zoom = zoom,
      )
    }
    AnimatedContent(
      modifier = Modifier.align(TopEnd),
      targetState = isEditing,
      transitionSpec = { fadeIn() togetherWith fadeOut() },
    ) { editing ->
      if (editing) {
        Text(
          modifier = Modifier.padding(4.dp),
          text = Strings.reportsFormulaEditorPreview.uppercase(),
          style = typography.labelMedium.copy(fontWeight = SemiBold, letterSpacing = 0.08.em),
          color = colors.tableHeaderText,
        )
      }
    }
  }

private val CONTENT_PADDING = 8.dp
private val PREVIEW_HEIGHT = 280.dp

@PortraitPreview
@Composable
private fun PreviewCustomReportScaffold(
  @PreviewParameter(CustomReportProvider::class) params: ColoredParams<CustomReportState>,
) = PreviewWithColoredParams(params) { CustomReportScaffold(state = this, onAction = {}) }

private class CustomReportProvider :
  ColoredParameterProvider<CustomReportState>(
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
