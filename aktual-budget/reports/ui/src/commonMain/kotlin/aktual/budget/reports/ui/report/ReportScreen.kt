package aktual.budget.reports.ui.report

import aktual.budget.model.WidgetId
import aktual.budget.reports.ui.Action
import aktual.budget.reports.ui.ActionListener
import aktual.budget.reports.ui.Tags
import aktual.budget.reports.ui.charts.ChartInfoSheet
import aktual.budget.reports.ui.charts.ChartZoomState
import aktual.budget.reports.ui.charts.PREVIEW_AGE_OF_MONEY_DATA
import aktual.budget.reports.ui.charts.ReportChart
import aktual.budget.reports.ui.charts.info
import aktual.budget.reports.ui.charts.rememberChartZoomState
import aktual.budget.reports.ui.montecarlo.MonteCarloReportScreen
import aktual.budget.reports.ui.string
import aktual.budget.reports.vm.AgeOfMoneyReportMeta
import aktual.budget.reports.vm.CustomData
import aktual.budget.reports.vm.dashboard.DashboardItem
import aktual.budget.reports.vm.dashboard.isRenamable
import aktual.budget.reports.vm.dashboard.name
import aktual.budget.reports.vm.report.ReportState
import aktual.budget.reports.vm.report.ReportViewModel
import aktual.core.icons.AktualIcons
import aktual.core.icons.InformationOutline
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.ZoomOut
import aktual.core.l10n.Strings
import aktual.core.nav.BackNavigator
import aktual.core.nav.TransactionsNavigator
import aktual.core.ui.AktualAlertDialog
import aktual.core.ui.AktualTheme.colors
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
import aktual.core.ui.hazedTopBar
import aktual.core.ui.hazedTopBarContent
import aktual.core.ui.hazedTopBarContentPadding
import aktual.core.ui.rememberHazedTopBarState
import aktual.core.ui.transparentTopAppBarColors
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
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
  if (state == MonteCarlo) {
    MonteCarloReportScreen(id = id, back = back)
    return
  }
  if (state == Formula) {
    FormulaReportScreen(id = id, back = back)
    return
  }
  if (state == Custom) {
    CustomReportScreen(id = id, back = back, toTransactions = toTransactions)
    return
  }
  if (state == BudgetAnalysis) {
    BudgetAnalysisReportScreen(id = id, back = back)
    return
  }

  var hasUnsavedText by rememberSaveable { mutableStateOf(false) }
  var showDiscardDialog by remember { mutableStateOf(false) }

  BackHandler(enabled = hasUnsavedText) { showDiscardDialog = true }

  ReportScaffold(
    state = state,
    onAction = { action ->
      @Suppress("ElseCaseInsteadOfExhaustiveWhen")
      when (action) {
        NavBack -> if (hasUnsavedText) showDiscardDialog = true else back()
        is Rename -> viewModel.rename(action.name)
        is SaveText -> viewModel.saveText(action.content, action.align)
        is SetUnsavedText -> hasUnsavedText = action.hasUnsavedText
        is SetSummaryType -> viewModel.setSummaryType(action.type)
        is SetAllTimeDivisor -> viewModel.setSummaryAllTimeDivisor(action.allTime)
        else -> Unit
      }
    },
  )

  if (showDiscardDialog) {
    DiscardChangesDialog(
      message = Strings.reportsTextDiscardMessage,
      onDiscard = {
        showDiscardDialog = false
        back()
      },
      onCancel = { showDiscardDialog = false },
    )
  }
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
) {
  val data = (state as? Loaded)?.data
  val zoom = rememberChartZoomState(data)
  val info = data?.info() ?: persistentListOf()
  var showInfo by remember { mutableStateOf(false) }
  val hazeState = rememberHazedTopBarState()

  if (showInfo) {
    ChartInfoSheet(info = info, onDismiss = { showInfo = false })
  }

  Scaffold(
    modifier = modifier.fillMaxSize(),
    topBar = {
      TopAppBar(
        modifier = Modifier.hazedTopBar(hazeState, scrollOffset = { 0f }),
        colors = colors.transparentTopAppBarColors(),
        navigationIcon = { NavBackIconButton(onClick = { onAction(NavBack) }) },
        title = {
          Text(
            text =
              when {
                data is CustomData && data.title.isNotBlank() -> data.title
                state is Loaded ->
                  state.item.name?.takeIf { it.isNotBlank() } ?: state.type.string()
                else -> Strings.reportsDashboardTitle
              },
          )
        },
        actions = {
          if (zoom.range != null) {
            BareIconButton(
              modifier = Modifier.testTag(Tags.ResetZoom),
              imageVector = MaterialIcons.ZoomOut,
              contentDescription = Strings.reportsResetZoom,
              onClick = zoom::reset,
            )
          }
          if (info.isNotEmpty()) {
            BareIconButton(
              modifier = Modifier.testTag(Tags.ChartInfo),
              imageVector = AktualIcons.InformationOutline,
              contentDescription = Strings.reportsChartInfo,
              onClick = { showInfo = true },
            )
          }
          if (state is Loaded && state.item.isRenamable) {
            ReportMenu(
              name = state.item.name.orEmpty(),
              onRename = { name -> onAction(Action.Rename(state.item, name)) },
            )
          }
        },
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
      Custom,
      BudgetAnalysis -> Box(modifier = contentModifier)

      is Loaded ->
        ReportChart(
          modifier = contentModifier.background(colors.tableBackground, CardShape).padding(8.dp),
          data = state.data,
          compact = false,
          onAction = onAction,
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
