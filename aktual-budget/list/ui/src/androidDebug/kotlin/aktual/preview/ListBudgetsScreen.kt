/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.preview

import aktual.budget.list.ui.ListBudgetsScaffold
import aktual.budget.list.vm.ListBudgetsState
import aktual.core.ui.PreviewParameters
import aktual.core.ui.PreviewThemedScreen
import aktual.core.ui.TripleScreenPreview
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewParameter
import kotlinx.collections.immutable.persistentListOf

@TripleScreenPreview
@Composable
private fun ScreenState(
  @PreviewParameter(StateParameters::class) state: ListBudgetsState,
) = PreviewThemedScreen {
  ListBudgetsScaffold(
    state = state,
    onAction = {},
  )
}

private class StateParameters : PreviewParameters<ListBudgetsState>(
  ListBudgetsState.Success(persistentListOf(PreviewBudgetSynced, PreviewBudgetSyncing, PreviewBudgetBroken)),
  ListBudgetsState.Loading,
  ListBudgetsState.Failure(reason = "Something broke lol"),
)
