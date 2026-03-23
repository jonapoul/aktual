package aktual.budget.sync.ui

import aktual.budget.sync.vm.KeyPasswordState
import aktual.budget.sync.vm.KeyPasswordState.Active
import aktual.budget.sync.vm.KeyPasswordState.Inactive
import aktual.budget.sync.vm.SyncOverallState
import aktual.budget.sync.vm.SyncStep
import aktual.budget.sync.vm.SyncStep.DownloadingDatabase
import aktual.budget.sync.vm.SyncStep.FetchingFileInfo
import aktual.budget.sync.vm.SyncStep.ValidatingDatabase
import aktual.budget.sync.vm.SyncStepState
import aktual.budget.sync.vm.SyncStepState.Failed
import aktual.budget.sync.vm.SyncStepState.InProgress.Definite
import aktual.budget.sync.vm.SyncStepState.InProgress.Indefinite
import aktual.budget.sync.vm.SyncStepState.NotStarted
import aktual.budget.sync.vm.SyncStepState.Succeeded
import aktual.core.model.Password
import aktual.core.model.percent
import aktual.core.ui.ThemedParameterProvider
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf

internal data class SyncBudgetDialogParams(
  val overallState: SyncOverallState,
  val passwordState: KeyPasswordState,
  val stepStates: ImmutableMap<SyncStep, SyncStepState>,
)

internal class SyncBudgetDialogProvider :
  ThemedParameterProvider<SyncBudgetDialogParams>(
    SyncBudgetDialogParams(
      overallState = SyncOverallState.NotStarted,
      passwordState = Inactive,
      stepStates =
        persistentMapOf(
          FetchingFileInfo to NotStarted,
          DownloadingDatabase to NotStarted,
          ValidatingDatabase to NotStarted,
        ),
    ),
    SyncBudgetDialogParams(
      overallState = SyncOverallState.InProgress,
      passwordState = Inactive,
      stepStates =
        persistentMapOf(
          FetchingFileInfo to Indefinite,
          DownloadingDatabase to Definite(50.percent),
          ValidatingDatabase to NotStarted,
        ),
    ),
    SyncBudgetDialogParams(
      overallState = SyncOverallState.Succeeded,
      passwordState = Inactive,
      stepStates =
        persistentMapOf(
          FetchingFileInfo to Succeeded,
          DownloadingDatabase to Succeeded,
          ValidatingDatabase to Succeeded,
        ),
    ),
    SyncBudgetDialogParams(
      overallState = SyncOverallState.Failed,
      passwordState = Inactive,
      stepStates =
        persistentMapOf(
          FetchingFileInfo to Failed("Some error"),
          DownloadingDatabase to Failed("Whatever"),
          ValidatingDatabase to
            Failed("Another error but this one's a lot longer, to see how it handles wrapping text"),
        ),
    ),
    SyncBudgetDialogParams(
      overallState = SyncOverallState.Failed,
      passwordState = Active(input = Password.Empty),
      stepStates =
        persistentMapOf(
          FetchingFileInfo to Succeeded,
          DownloadingDatabase to Succeeded,
          ValidatingDatabase to Failed("Missing Key"),
        ),
    ),
  )
