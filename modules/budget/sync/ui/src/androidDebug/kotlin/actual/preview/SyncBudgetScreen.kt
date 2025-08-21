package actual.preview

import actual.core.model.Password
import actual.budget.sync.ui.SyncBudgetScaffold
import actual.budget.sync.vm.KeyPasswordState
import actual.budget.sync.vm.SyncStep
import actual.budget.sync.vm.SyncStepState
import actual.core.model.percent
import actual.core.ui.PreviewThemedScreen
import actual.core.ui.TripleScreenPreview
import androidx.compose.runtime.Composable
import kotlinx.collections.immutable.persistentMapOf

@TripleScreenPreview
@Composable
private fun NotStarted() = PreviewThemedScreen {
  SyncBudgetScaffold(
    onAction = {},
    passwordState = KeyPasswordState.Inactive,
    stepStates = persistentMapOf(
      SyncStep.FetchingFileInfo to SyncStepState.NotStarted,
      SyncStep.DownloadingDatabase to SyncStepState.NotStarted,
      SyncStep.ValidatingDatabase to SyncStepState.NotStarted,
    ),
  )
}

@TripleScreenPreview
@Composable
private fun Downloading() = PreviewThemedScreen {
  SyncBudgetScaffold(
    onAction = {},
    passwordState = KeyPasswordState.Inactive,
    stepStates = persistentMapOf(
      SyncStep.FetchingFileInfo to SyncStepState.InProgress.Indefinite,
      SyncStep.DownloadingDatabase to SyncStepState.InProgress.Definite(50.percent),
      SyncStep.ValidatingDatabase to SyncStepState.NotStarted,
    ),
  )
}

@TripleScreenPreview
@Composable
private fun AllSucceeded() = PreviewThemedScreen {
  SyncBudgetScaffold(
    onAction = {},
    passwordState = KeyPasswordState.Inactive,
    stepStates = persistentMapOf(
      SyncStep.FetchingFileInfo to SyncStepState.Succeeded,
      SyncStep.DownloadingDatabase to SyncStepState.Succeeded,
      SyncStep.ValidatingDatabase to SyncStepState.Succeeded,
    ),
  )
}

@TripleScreenPreview
@Composable
private fun AllFailed() = PreviewThemedScreen {
  SyncBudgetScaffold(
    onAction = {},
    passwordState = KeyPasswordState.Inactive,
    stepStates = persistentMapOf(
      SyncStep.FetchingFileInfo to SyncStepState.Failed("Some error"),
      SyncStep.DownloadingDatabase to SyncStepState.Failed("Whatever"),
      SyncStep.ValidatingDatabase to SyncStepState.Failed(
        "Another error but this one's a lot longer, to see how it handles wrapping text",
      ),
    ),
  )
}

@TripleScreenPreview
@Composable
private fun ShowPasswordInputDialog() = PreviewThemedScreen {
  SyncBudgetScaffold(
    onAction = {},
    passwordState = KeyPasswordState.Active(input = Password.Empty),
    stepStates = persistentMapOf(
      SyncStep.FetchingFileInfo to SyncStepState.Succeeded,
      SyncStep.DownloadingDatabase to SyncStepState.Succeeded,
      SyncStep.ValidatingDatabase to SyncStepState.Failed("Missing Key"),
    ),
  )
}
