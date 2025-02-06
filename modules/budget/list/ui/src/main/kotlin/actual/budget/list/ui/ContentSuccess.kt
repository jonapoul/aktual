package actual.budget.list.ui

import actual.budget.list.vm.Budget
import actual.core.ui.PreviewActualScreen
import actual.core.ui.ActualScreenPreview
import actual.core.ui.LocalTheme
import actual.core.ui.Theme
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Stable
@Composable
internal fun ContentSuccess(
  budgets: ImmutableList<Budget>,
  onClickOpen: (Budget) -> Unit,
  onClickDelete: (Budget) -> Unit,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  LazyColumn(
    modifier = modifier.fillMaxWidth(),
  ) {
    items(budgets) { budget ->
      BudgetListItem(
        budget = budget,
        theme = theme,
        onClickOpen = { onClickOpen(budget) },
        onClickDelete = { onClickDelete(budget) },
      )
    }
  }
}

@ActualScreenPreview
@Composable
private fun One() = PreviewActualScreen {
  ContentSuccess(
    budgets = persistentListOf(PreviewBudget),
    onClickOpen = {},
    onClickDelete = {},
  )
}

@ActualScreenPreview
@Composable
private fun Multiple() = PreviewActualScreen {
  ContentSuccess(
    budgets = persistentListOf(PreviewBudget, PreviewBudget, PreviewBudget),
    onClickOpen = {},
    onClickDelete = {},
  )
}
