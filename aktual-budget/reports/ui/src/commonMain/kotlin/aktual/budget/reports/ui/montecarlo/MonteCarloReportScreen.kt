package aktual.budget.reports.ui.montecarlo

import aktual.budget.model.WidgetId
import aktual.budget.reports.vm.montecarlo.MonteCarloResultsView
import aktual.budget.reports.vm.montecarlo.MonteCarloState
import aktual.budget.reports.vm.montecarlo.MonteCarloViewModel
import aktual.budget.reports.vm.montecarlo.previewMonteCarloState
import aktual.core.l10n.Strings
import aktual.core.nav.BackNavigator
import aktual.core.ui.AktualAlertDialog
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.BackHandler
import aktual.core.ui.BottomSpacing
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParams
import aktual.core.ui.NavBackIconButton
import aktual.core.ui.PageBackground
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColoredParams
import aktual.core.ui.transparentTopAppBarColors
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel

@Composable
fun MonteCarloReportScreen(
  id: WidgetId,
  back: BackNavigator,
  modifier: Modifier = Modifier,
  viewModel: MonteCarloViewModel = monteCarloViewModel(id),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  var showDiscardDialog by remember { mutableStateOf(false) }
  val hasChanges = (state as? MonteCarloState.Loaded)?.hasChanges == true

  BackHandler(enabled = hasChanges) { showDiscardDialog = true }

  MonteCarloScaffold(
    modifier = modifier,
    state = state,
    onAction = { action ->
      when (action) {
        MonteCarloAction.NavBack -> if (hasChanges) showDiscardDialog = true else back()
        MonteCarloAction.Save -> viewModel.save()
        is MonteCarloAction.SetConfig -> viewModel.setConfig(action.config)
        is MonteCarloAction.SetShowTodaysMoney -> viewModel.setShowTodaysMoney(action.show)
        is MonteCarloAction.SetResultsView -> viewModel.setResultsView(action.view)
        is MonteCarloAction.SetGraphView -> viewModel.setGraphView(action.view)
        is MonteCarloAction.SetCashflowPercentile ->
          viewModel.setCashflowPercentile(action.percentile)
        is MonteCarloAction.SelectRun -> viewModel.selectRun(action.index)
      }
    },
  )

  if (showDiscardDialog) {
    AktualAlertDialog(
      title = Strings.monteCarloDiscardTitle,
      onDismissRequest = { showDiscardDialog = false },
      buttons = {
        TextButton(onClick = { showDiscardDialog = false }) {
          Text(Strings.monteCarloDiscardCancel)
        }
        TextButton(
          onClick = {
            showDiscardDialog = false
            viewModel.reset()
            back()
          }
        ) {
          Text(Strings.monteCarloDiscardConfirm, color = colors.errorText)
        }
      },
      content = { Text(Strings.monteCarloDiscardMessage) },
    )
  }
}

@Composable
private fun monteCarloViewModel(id: WidgetId) =
  assistedMetroViewModel<MonteCarloViewModel, MonteCarloViewModel.Factory>(key = id.value) {
    create(id)
  }

@Composable
internal fun MonteCarloScaffold(
  state: MonteCarloState,
  onAction: MonteCarloActionListener,
  modifier: Modifier = Modifier,
) =
  Scaffold(
    modifier = modifier.fillMaxSize(),
    topBar = {
      TopAppBar(
        colors = colors.transparentTopAppBarColors(),
        navigationIcon = { NavBackIconButton(onClick = { onAction(MonteCarloAction.NavBack) }) },
        title = {
          Text(
            text =
              (state as? MonteCarloState.Loaded)?.title?.takeIf { it.isNotEmpty() }
                ?: Strings.reportsChooseTypeMonteCarlo
          )
        },
        actions = {
          if (state is MonteCarloState.Loaded && state.hasChanges) {
            TextButton(onClick = { onAction(MonteCarloAction.Save) }) {
              Text(Strings.monteCarloSave, color = colors.pageTextPositive)
            }
          }
        },
      )
    },
  ) { innerPadding ->
    Box(modifier = Modifier.fillMaxSize()) {
      PageBackground()
      when (state) {
        MonteCarloState.Loading ->
          Box(modifier = Modifier.fillMaxSize(), contentAlignment = Center) {
            CircularProgressIndicator(color = colors.pageText)
          }

        MonteCarloState.NotFound ->
          Box(modifier = Modifier.fillMaxSize(), contentAlignment = Center) {
            Text(text = Strings.reportsNotFound, color = colors.pageText)
          }

        is MonteCarloState.Loaded ->
          Column(
            modifier =
              Modifier.padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            MonteCarloResults(state = state, onAction = onAction)
            BottomSpacing()
          }
      }
    }
  }

@PortraitPreview
@Composable
private fun PreviewMonteCarloScaffold(
  @PreviewParameter(MonteCarloScaffoldProvider::class) params: ColoredParams<MonteCarloState>
) = PreviewWithColoredParams(params) { MonteCarloScaffold(state = this, onAction = {}) }

private class MonteCarloScaffoldProvider :
  ColoredParameterProvider<MonteCarloState>(
    previewMonteCarloState(),
    previewMonteCarloState(resultsView = MonteCarloResultsView.Cashflow),
    previewMonteCarloState(resultsView = MonteCarloResultsView.Runs),
    MonteCarloState.Loading,
  )
