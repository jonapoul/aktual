package aktual.budget.reports.ui.montecarlo

import aktual.budget.model.WidgetId
import aktual.budget.reports.ui.dashboard.NameDialog
import aktual.budget.reports.vm.montecarlo.MonteCarloSection
import aktual.budget.reports.vm.montecarlo.MonteCarloState
import aktual.budget.reports.vm.montecarlo.MonteCarloViewModel
import aktual.budget.reports.vm.montecarlo.previewMonteCarloState
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.MoreVert
import aktual.core.l10n.Strings
import aktual.core.nav.BackNavigator
import aktual.core.ui.AktualAlertDialog
import aktual.core.ui.AktualDropdownMenu
import aktual.core.ui.AktualDropdownMenuItem
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.BackHandler
import aktual.core.ui.BareIconButton
import aktual.core.ui.BottomSpacing
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParams
import aktual.core.ui.NavBackIconButton
import aktual.core.ui.PageBackground
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColoredParams
import aktual.core.ui.checkbox
import aktual.core.ui.transparentTopAppBarColors
import aktual.core.ui.verticalScrollWithBar
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.launch

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
  val focusManager = LocalFocusManager.current
  val scope = rememberCoroutineScope()

  // A field being edited commits its text when it loses focus, which can land a frame later
  val afterCommit: (() -> Unit) -> Unit = { block ->
    focusManager.clearFocus()
    scope.launch {
      withFrameNanos {}
      block()
    }
  }

  BackHandler(enabled = hasChanges) { showDiscardDialog = true }

  MonteCarloScaffold(
    modifier = modifier,
    state = state,
    onAction = { action ->
      when (action) {
        NavBack ->
          afterCommit {
            if (viewModel.hasUnsavedChanges()) showDiscardDialog = true else back()
          }
        Save -> afterCommit { viewModel.save() }
        is Rename -> viewModel.rename(action.name)
        is Edit -> viewModel.edit(action.transform)
        AddPot -> viewModel.addPot()
        AddIncomeStream -> viewModel.addIncomeStream()
        AddContribution -> viewModel.addContribution()
        AddSpendingPhase -> viewModel.addSpendingPhase()
        AddTaxBand -> viewModel.addTaxBand()
        is SetKeepSurplus -> viewModel.setKeepSurplus(action.keep)
        is SetShowTodaysMoney -> viewModel.setShowTodaysMoney(action.show)
        is ToggleSection -> afterCommit { viewModel.toggleSection(action.section) }
        is SetResultsView -> viewModel.setResultsView(action.view)
        is SetGraphView -> viewModel.setGraphView(action.view)
        is SetCashflowPercentile -> viewModel.setCashflowPercentile(action.percentile)
        is SelectRun -> viewModel.selectRun(action.index)
      }
    },
  )

  if (showDiscardDialog) {
    AktualAlertDialog(
      title = Strings.monteCarloDiscardTitle,
      highlight = colors.warningText,
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
          },
        ) {
          Text(Strings.monteCarloDiscardConfirm, color = colors.warningText)
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
  onAction: MonteCarloActionHandler,
  modifier: Modifier = Modifier,
) {
  val focusManager = LocalFocusManager.current
  Scaffold(
    modifier = modifier.fillMaxSize(),
    topBar = {
      TopAppBar(
        colors = colors.transparentTopAppBarColors(),
        navigationIcon = { NavBackIconButton(onClick = { onAction(NavBack) }) },
        title = {
          Text(
            text =
              (state as? MonteCarloState.Loaded)?.title?.takeIf { it.isNotEmpty() }
                ?: Strings.reportsChooseTypeMonteCarlo,
          )
        },
        actions = {
          if (state is Loaded) {
            if (state.hasChanges) {
              TextButton(onClick = { onAction(Save) }) {
                Text(Strings.monteCarloSave, color = colors.pageTextPositive)
              }
            }
            MonteCarloMenu(
              title = state.title.orEmpty(),
              showTodaysMoney = state.showTodaysMoney,
              onAction = onAction,
            )
          }
        },
      )
    },
  ) { innerPadding ->
    Box(modifier = Modifier.fillMaxSize()) {
      PageBackground()
      when (state) {
        Loading ->
          Box(modifier = Modifier.fillMaxSize(), contentAlignment = Center) {
            CircularProgressIndicator(color = colors.pageText)
          }

        NotFound ->
          Box(modifier = Modifier.fillMaxSize(), contentAlignment = Center) {
            Text(text = Strings.reportsNotFound, color = colors.pageText)
          }

        is Loaded ->
          Column(
            modifier =
              Modifier.padding(innerPadding)
                .verticalScrollWithBar()
                // Tapping away from a field ends its edit
                .pointerInput(Unit) { detectTapGestures { focusManager.clearFocus() } }
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            MonteCarloConfiguration(state = state, onAction = onAction)
            MonteCarloResults(state = state, onAction = onAction)
            BottomSpacing()
          }
      }
    }
  }
}

@Composable
private fun MonteCarloMenu(
  title: String,
  showTodaysMoney: Boolean,
  onAction: MonteCarloActionHandler,
  modifier: Modifier = Modifier,
) {
  var expanded by remember { mutableStateOf(false) }
  var showRenameDialog by remember { mutableStateOf(false) }

  if (showRenameDialog) {
    NameDialog(
      title = Strings.reportsDashboardRenameReport,
      placeholder = Strings.reportsDashboardReportName,
      confirmText = Strings.reportsDashboardNameSave,
      initialName = title,
      onConfirm = { name ->
        showRenameDialog = false
        onAction(MonteCarloAction.Rename(name))
      },
      onDismiss = { showRenameDialog = false },
    )
  }

  Box(modifier = modifier) {
    BareIconButton(
      imageVector = MaterialIcons.MoreVert,
      contentDescription = Strings.monteCarloMenu,
      onClick = { expanded = true },
    )

    AktualDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
      AktualDropdownMenuItem(
        text = { Text(Strings.reportsDashboardRename) },
        onClick = {
          expanded = false
          showRenameDialog = true
        },
      )
      AktualDropdownMenuItem(
        text = { Text(Strings.monteCarloTodaysMoney) },
        leadingIcon = {
          Checkbox(checked = showTodaysMoney, onCheckedChange = null, colors = colors.checkbox())
        },
        onClick = {
          expanded = false
          onAction(MonteCarloAction.SetShowTodaysMoney(!showTodaysMoney))
        },
      )
    }
  }
}

@PortraitPreview
@Composable
private fun PreviewMonteCarloScaffold(
  @PreviewParameter(MonteCarloScaffoldProvider::class) params: ColoredParams<MonteCarloState>,
) = PreviewWithColoredParams(params) { MonteCarloScaffold(state = this, onAction = {}) }

private class MonteCarloScaffoldProvider :
  ColoredParameterProvider<MonteCarloState>(
    previewMonteCarloState(),
    previewMonteCarloState(resultsView = Cashflow),
    previewMonteCarloState(resultsView = Runs),
    previewMonteCarloState(
      collapsedSections =
        persistentSetOf<MonteCarloSection>(Configuration, Depletion, MonteCarloSection.HowItWorks),
    ),
    Loading,
  )
