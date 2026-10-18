package aktual.budget.reports.ui.report

import aktual.budget.model.DashboardPageId
import aktual.budget.model.WidgetId
import aktual.budget.reports.ui.dashboard.NameDialog
import aktual.budget.reports.vm.report.CustomReportState
import aktual.budget.reports.vm.report.NewCustomReportViewModel
import aktual.core.l10n.Strings
import aktual.core.nav.BackNavigator
import aktual.core.ui.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import kotlinx.coroutines.flow.collectLatest

// Builds a custom report from upstream's defaults. Saving asks for a name, then opens the report
@Composable
internal fun NewCustomReportScreen(
  page: DashboardPageId,
  back: BackNavigator,
  toSavedReport: (WidgetId) -> Unit,
  viewModel: NewCustomReportViewModel = newCustomReportViewModel(page),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  val editorData by viewModel.editorData.collectAsStateWithLifecycle()
  val takenNames by viewModel.takenNames.collectAsStateWithLifecycle()
  var showDiscard by rememberSaveable { mutableStateOf(false) }
  var showSave by rememberSaveable { mutableStateOf(false) }
  val loaded = state as? CustomReportState.Loaded
  val isEditing = loaded?.isEditing == true

  val currentToSavedReport by rememberUpdatedState(toSavedReport)
  LaunchedEffect(Unit) { viewModel.savedEvent.collectLatest { currentToSavedReport(it) } }

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
        Save -> showSave = true
        is Edit -> viewModel.edit(action.transform)
      }
    },
  )

  if (showSave) {
    NameDialog(
      title = Strings.reportsCustomEditorSaveNew,
      placeholder = Strings.reportsDashboardReportName,
      confirmText = Strings.reportsDashboardNameSave,
      error = { name -> if (name in takenNames) Strings.reportsCustomEditorNameTaken else null },
      onConfirm = { name ->
        showSave = false
        viewModel.save(name)
      },
      onDismiss = { showSave = false },
    )
  }

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
private fun newCustomReportViewModel(page: DashboardPageId) =
  assistedMetroViewModel<NewCustomReportViewModel, NewCustomReportViewModel.Factory>(
    key = page.value,
  ) {
    create(page)
  }
