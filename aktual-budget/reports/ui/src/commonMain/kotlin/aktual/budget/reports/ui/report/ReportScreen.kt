package aktual.budget.reports.ui.report

import aktual.budget.model.WidgetId
import aktual.budget.reports.components.ChartInfoSheet
import aktual.budget.reports.components.ChartTags
import aktual.budget.reports.components.ChartZoomState
import aktual.budget.reports.components.PREVIEW_AGE_OF_MONEY_DATA
import aktual.budget.reports.components.ReportChart
import aktual.budget.reports.components.info
import aktual.budget.reports.components.rememberChartZoomState
import aktual.budget.reports.components.string
import aktual.budget.reports.ui.Action
import aktual.budget.reports.ui.ActionListener
import aktual.budget.reports.ui.Tags
import aktual.budget.reports.ui.montecarlo.MonteCarloReportScreen
import aktual.budget.reports.vm.AgeOfMoneyReportMeta
import aktual.budget.reports.vm.CustomData
import aktual.budget.reports.vm.QueryEditorData
import aktual.budget.reports.vm.SummaryReportMeta
import aktual.budget.reports.vm.dashboard.DashboardItem
import aktual.budget.reports.vm.dashboard.isRenamable
import aktual.budget.reports.vm.dashboard.name
import aktual.budget.reports.vm.report.ReportState
import aktual.budget.reports.vm.report.ReportViewModel
import aktual.budget.reports.vm.withAllTimeDivisor
import aktual.budget.reports.vm.withSummaryType
import aktual.core.icons.AktualIcons
import aktual.core.icons.Delete
import aktual.core.icons.Tuning
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.ZoomOut
import aktual.core.l10n.Strings
import aktual.core.nav.BackNavigator
import aktual.core.nav.TransactionsNavigator
import aktual.core.ui.AktualAlertDialog
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.BackHandler
import aktual.core.ui.BareIconButton
import aktual.core.ui.BottomSpacing
import aktual.core.ui.CardShape
import aktual.core.ui.ColorParams
import aktual.core.ui.ColorParamsProvider
import aktual.core.ui.Dimens
import aktual.core.ui.NavBackIconButton
import aktual.core.ui.PageBackground
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColorParams
import aktual.core.ui.PrimaryTextButton
import aktual.core.ui.hazedTopBar
import aktual.core.ui.hazedTopBarContent
import aktual.core.ui.hazedTopBarContentPadding
import aktual.core.ui.rememberHazedTopBarState
import aktual.core.ui.transparentTopAppBarColors
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import kotlinx.collections.immutable.persistentListOf

@Composable
fun ReportScreen(
  id: WidgetId,
  back: BackNavigator,
  toTransactions: TransactionsNavigator,
  viewModel: ReportViewModel = reportViewModel(id),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  if (showOwnScreen(state, id, back, toTransactions)) return

  var hasUnsavedText by rememberSaveable { mutableStateOf(false) }
  var showDiscardDialog by remember { mutableStateOf(false) }
  val calendarDrilldown = rememberCalendarDrilldown(toTransactions)

  val loaded = state as? Loaded
  BackHandler(enabled = hasUnsavedText || loaded?.hasChanges == true) { showDiscardDialog = true }

  // Only the settings sheets pick from it
  val editorData =
    if (loaded?.draft?.hasSettings() == true) {
      viewModel.queryEditorData.collectAsStateWithLifecycle().value
    } else {
      null
    }

  ReportScaffold(
    state = state,
    editorData = editorData,
    onAction = { action ->
      @Suppress("ElseCaseInsteadOfExhaustiveWhen")
      when (action) {
        NavBack ->
          if (hasUnsavedText || viewModel.hasUnsavedChanges()) showDiscardDialog = true else back()
        is Rename -> viewModel.rename(action.name)
        is EditSettings -> viewModel.edit(action.transform)
        SaveSettings -> viewModel.save()
        is Chart ->
          when (val chart = action.action) {
            is SaveText -> viewModel.saveText(chart.content, chart.align)
            is SetUnsavedText -> hasUnsavedText = chart.hasUnsavedText
            is SetSummaryType -> viewModel.editSummary { it.withSummaryType(chart.type) }
            is SetAllTimeDivisor -> viewModel.editSummary { it.withAllTimeDivisor(chart.allTime) }
            is ClickCalendarDay -> calendarDrilldown(chart)
            is DrillDown -> Unit
          }
        else -> Unit
      }
    },
  )

  if (showDiscardDialog) {
    DiscardChangesDialog(
      message =
        if (hasUnsavedText) {
          Strings.reportsTextDiscardMessage
        } else {
          Strings.reportsCustomEditorDiscardMessage
        },
      onDiscard = {
        showDiscardDialog = false
        viewModel.discard()
        back()
      },
      onCancel = { showDiscardDialog = false },
    )
  }
}

private fun ReportViewModel.editSummary(transform: (SummaryReportMeta) -> SummaryReportMeta) =
  edit { meta ->
    if (meta is SummaryReportMeta) transform(meta) else meta
  }

// Some reports have their own screen, with its own view model. Returns whether it showed one
@Composable
private fun showOwnScreen(
  state: ReportState,
  id: WidgetId,
  back: BackNavigator,
  toTransactions: TransactionsNavigator,
): Boolean {
  when (state) {
    MonteCarlo -> MonteCarloReportScreen(id = id, back = back)
    Formula -> FormulaReportScreen(id = id, back = back)
    Custom -> CustomReportScreen(id = id, back = back, toTransactions = toTransactions)
    Loading,
    NotFound,
    is Loaded -> return false
  }
  return true
}

@Composable
internal fun DiscardChangesDialog(message: String, onDiscard: () -> Unit, onCancel: () -> Unit) =
  AktualAlertDialog(
    title = Strings.reportsTextDiscardTitle,
    highlight = colors.warningText,
    onDismissRequest = onCancel,
    buttons = {
      TextButton(onClick = onCancel) { Text(Strings.reportsTextDiscardCancel) }
      TextButton(onClick = onDiscard) {
        Text(Strings.reportsTextDiscardConfirm, color = colors.warningText)
      }
    },
    content = { Text(message) },
  )

@Composable
private fun reportViewModel(id: WidgetId) =
  assistedMetroViewModel<ReportViewModel, ReportViewModel.Factory>(key = id.value) { create(id) }

@Composable
internal fun ReportScaffold(
  state: ReportState,
  onAction: ActionListener,
  modifier: Modifier = Modifier,
  editorData: QueryEditorData? = null,
) {
  val loaded = state as? Loaded
  val data = loaded?.data
  val zoom = rememberChartZoomState(data)
  val info = data?.info() ?: persistentListOf()
  var showInfo by remember { mutableStateOf(false) }
  var showSettings by remember { mutableStateOf(false) }
  val hazeState = rememberHazedTopBarState()

  if (showInfo) {
    ChartInfoSheet(info = info, onDismiss = { showInfo = false })
  }

  if (showSettings && loaded != null) {
    ReportSettingsSheet(
      draft = loaded.draft,
      editorData = editorData,
      onEdit = { transform -> onAction(Action.EditSettings(transform)) },
      onDismiss = { showSettings = false },
    )
  }

  Scaffold(
    modifier = modifier.fillMaxSize(),
    topBar = {
      ReportTopBar(
        modifier = Modifier.hazedTopBar(hazeState, scrollOffset = { 0f }),
        loaded = loaded,
        zoom = zoom,
        hasInfo = info.isNotEmpty(),
        onShowInfo = { showInfo = true },
        onShowSettings = { showSettings = true },
        onSave = {
          showSettings = false
          onAction(SaveSettings)
        },
        onAction = onAction,
      )
    },
  ) { innerPadding ->
    Box(modifier = Modifier.fillMaxSize()) {
      PageBackground()
      ReportContent(
        modifier = Modifier.hazedTopBarContent(hazeState, innerPadding).fillMaxSize(),
        contentPadding = hazedTopBarContentPadding(hazeState, innerPadding),
        state = state,
        zoom = zoom,
        onAction = onAction,
      )
    }
  }
}

// Unsaved changes swap back for discard, and show save
@Composable
private fun ReportTopBar(
  loaded: ReportState.Loaded?,
  zoom: ChartZoomState,
  hasInfo: Boolean,
  onShowInfo: () -> Unit,
  onShowSettings: () -> Unit,
  onSave: () -> Unit,
  onAction: ActionListener,
  modifier: Modifier = Modifier,
) {
  val data = loaded?.data
  val hasChanges = loaded?.hasChanges == true
  TopAppBar(
    modifier = modifier,
    colors = colors.transparentTopAppBarColors(),
    navigationIcon = {
      if (hasChanges) {
        IconButton(
          modifier = Modifier.testTag(Tags.DiscardReport),
          onClick = { onAction(NavBack) },
        ) {
          Icon(
            imageVector = AktualIcons.Delete,
            contentDescription = Strings.reportsTextDiscard,
          )
        }
      } else {
        NavBackIconButton(onClick = { onAction(NavBack) })
      }
    },
    title = {
      Column {
        Text(
          text =
            when {
              data is CustomData && data.title.isNotBlank() -> data.title
              loaded != null -> loaded.item.name?.takeIf { it.isNotBlank() } ?: loaded.type.string()
              else -> Strings.reportsDashboardTitle
            },
        )
        AnimatedVisibility(visible = hasChanges) {
          Text(
            text = Strings.reportsCustomEditorUnsaved,
            style = typography.labelMedium,
            color = colors.warningText,
          )
        }
      }
    },
    actions = {
      if (hasChanges) {
        PrimaryTextButton(
          modifier = Modifier.padding(end = 8.dp).testTag(Tags.SaveReport),
          text = Strings.reportsFormulaEditorSave,
          shape = CircleShape,
          onClick = onSave,
        )
      }
      if (zoom.range != null) {
        BareIconButton(
          modifier = Modifier.testTag(ChartTags.ResetZoom),
          imageVector = MaterialIcons.ZoomOut,
          contentDescription = Strings.reportsResetZoom,
          onClick = zoom::reset,
        )
      }
      if (loaded?.draft?.hasSettings() == true) {
        BareIconButton(
          modifier = Modifier.testTag(ChartTags.ChartSettings),
          imageVector = AktualIcons.Tuning,
          contentDescription = Strings.reportsChartSettings,
          onClick = onShowSettings,
        )
      }
      val canRename = loaded != null && loaded.item.isRenamable
      if (loaded != null && (canRename || hasInfo)) {
        ReportMenu(
          name = loaded.item.name.orEmpty(),
          onRename =
            if (canRename) {
              { name -> onAction(Action.Rename(loaded.item, name)) }
            } else {
              null
            },
          onShowInfo = if (hasInfo) onShowInfo else null,
        )
      }
    },
  )
}

@Composable
private fun ReportContent(
  state: ReportState,
  contentPadding: PaddingValues,
  zoom: ChartZoomState,
  onAction: ActionListener,
  modifier: Modifier = Modifier,
) {
  Column(modifier = modifier.padding(contentPadding).padding(Dimens.ScreenPadding)) {
    val contentModifier = Modifier.fillMaxWidth().weight(1f)
    when (state) {
      Loading ->
        Box(modifier = contentModifier, contentAlignment = Center) {
          CircularProgressIndicator(color = colors.pageText)
        }

      NotFound ->
        Box(modifier = contentModifier, contentAlignment = Center) {
          Text(text = Strings.reportsNotFound, color = colors.pageText)
        }

      // Shown by their own screens instead
      MonteCarlo,
      Formula,
      Custom -> Box(modifier = contentModifier)

      is Loaded ->
        ReportChart(
          modifier = contentModifier.background(colors.tableBackground, CardShape).padding(8.dp),
          data = state.data,
          compact = false,
          onAction = { onAction(Action.Chart(it)) },
          zoom = zoom,
        )
    }
    BottomSpacing()
  }
}

@PortraitPreview
@Composable
private fun PreviewReportScaffold(
  @PreviewParameter(ReportScaffoldProvider::class) params: ColorParams<ReportState>,
) = PreviewWithColorParams(params) { ReportScaffold(state = this, onAction = {}) }

private class ReportScaffoldProvider :
  ColorParamsProvider<ReportState>(
    ReportState.Loaded(
      type = AgeOfMoney,
      item = DashboardItem(id = WidgetId("preview"), x = 0, y = 0, meta = AgeOfMoneyReportMeta()),
      data = PREVIEW_AGE_OF_MONEY_DATA,
    ),
    Loading,
    NotFound,
  )
