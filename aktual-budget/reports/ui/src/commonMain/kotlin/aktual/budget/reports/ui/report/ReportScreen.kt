package aktual.budget.reports.ui.report

import aktual.budget.model.WidgetId
import aktual.budget.reports.ui.ActionListener
import aktual.budget.reports.ui.Tags
import aktual.budget.reports.ui.charts.ChartZoomState
import aktual.budget.reports.ui.charts.PREVIEW_AGE_OF_MONEY_DATA
import aktual.budget.reports.ui.charts.ReportChart
import aktual.budget.reports.ui.charts.rememberChartZoomState
import aktual.budget.reports.ui.montecarlo.MonteCarloReportScreen
import aktual.budget.reports.ui.string
import aktual.budget.reports.vm.AgeOfMoneyReportMeta
import aktual.budget.reports.vm.dashboard.DashboardItem
import aktual.budget.reports.vm.report.ReportState
import aktual.budget.reports.vm.report.ReportViewModel
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.ZoomOut
import aktual.core.l10n.Strings
import aktual.core.nav.BackNavigator
import aktual.core.ui.AktualAlertDialog
import aktual.core.ui.AktualTheme.colors
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
import aktual.core.ui.transparentTopAppBarColors
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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

@Composable
fun ReportScreen(
  id: WidgetId,
  back: BackNavigator,
  viewModel: ReportViewModel = reportViewModel(id),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  if (state == MonteCarlo) {
    MonteCarloReportScreen(id = id, back = back)
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
        is SaveText -> viewModel.saveText(action.content, action.align)
        is SetUnsavedText -> hasUnsavedText = action.hasUnsavedText
        else -> Unit
      }
    },
  )

  if (showDiscardDialog) {
    DiscardTextDialog(
      onDiscard = {
        showDiscardDialog = false
        back()
      },
      onCancel = { showDiscardDialog = false },
    )
  }
}

@Composable
private fun DiscardTextDialog(onDiscard: () -> Unit, onCancel: () -> Unit) =
  AktualAlertDialog(
    title = Strings.reportsTextDiscardTitle,
    onDismissRequest = onCancel,
    buttons = {
      TextButton(onClick = onCancel) { Text(Strings.reportsTextDiscardCancel) }
      TextButton(onClick = onDiscard) {
        Text(Strings.reportsTextDiscardConfirm, color = colors.errorText)
      }
    },
    content = { Text(Strings.reportsTextDiscardMessage) },
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
  val zoom = rememberChartZoomState((state as? Loaded)?.data)

  Scaffold(
    modifier = modifier.fillMaxSize(),
    topBar = {
      TopAppBar(
        colors = colors.transparentTopAppBarColors(),
        navigationIcon = { NavBackIconButton(onClick = { onAction(NavBack) }) },
        title = {
          Text(
            text =
              if (state is Loaded) {
                state.type.string()
              } else {
                Strings.reportsDashboardTitle
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
        },
      )
    },
  ) { innerPadding ->
    Box(modifier = Modifier.fillMaxSize()) {
      PageBackground()
      Column(modifier = Modifier.padding(innerPadding).padding(horizontal = 8.dp)) {
        ReportContent(
          modifier = Modifier.padding(top = 8.dp, bottom = 8.dp).fillMaxWidth().weight(1f),
          state = state,
          zoom = zoom,
          onAction = onAction,
        )
        BottomSpacing()
      }
    }
  }
}

@Composable
private fun ReportContent(
  state: ReportState,
  zoom: ChartZoomState,
  onAction: ActionListener,
  modifier: Modifier = Modifier,
) =
  when (state) {
    Loading ->
      Box(modifier = modifier, contentAlignment = Center) {
        CircularProgressIndicator(color = colors.pageText)
      }

    NotFound ->
      Box(modifier = modifier, contentAlignment = Center) {
        Text(text = Strings.reportsNotFound, color = colors.pageText)
      }

    // Shown by MonteCarloReportScreen instead
    MonteCarlo -> Box(modifier = modifier)

    is Loaded ->
      ReportChart(
        modifier = modifier.background(colors.tableBackground, CardShape).padding(8.dp),
        data = state.data,
        compact = false,
        onAction = onAction,
        zoom = zoom,
      )
  }

@PortraitPreview
@Composable
private fun PreviewReportScaffold(
  @PreviewParameter(ReportScaffoldProvider::class) params: ColoredParams<ReportState>,
) = PreviewWithColoredParams(params) { ReportScaffold(state = this, onAction = {}) }

private class ReportScaffoldProvider :
  ColoredParameterProvider<ReportState>(
    ReportState.Loaded(
      type = AgeOfMoney,
      item = DashboardItem(id = WidgetId("preview"), x = 0, y = 0, meta = AgeOfMoneyReportMeta()),
      data = PREVIEW_AGE_OF_MONEY_DATA,
    ),
    Loading,
    NotFound,
  )
