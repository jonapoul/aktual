/**
 * Copyright 2025 Jon Poulton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package aktual.preview

import aktual.budget.sync.ui.SyncBudgetScaffold
import aktual.budget.sync.vm.KeyPasswordState
import aktual.budget.sync.vm.SyncStep
import aktual.budget.sync.vm.SyncStepState
import aktual.core.model.Password
import aktual.core.model.percent
import aktual.core.ui.PreviewThemedScreen
import aktual.core.ui.TripleScreenPreview
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
