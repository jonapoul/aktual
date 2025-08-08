package actual.preview

import actual.budget.list.ui.ListBudgetsScaffold
import actual.budget.list.vm.ListBudgetsState
import actual.core.ui.PreviewParameters
import actual.core.ui.PreviewThemedScreen
import actual.core.ui.TripleScreenPreview
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
  ListBudgetsState.Failure(reason = "Something broke lol")
)
