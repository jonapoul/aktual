package actual.budget.list.ui

import actual.budget.list.vm.Budget
import actual.core.ui.LocalTheme
import actual.core.ui.PreviewScreen
import actual.core.ui.ScreenPreview
import actual.core.ui.Theme
import androidx.compose.foundation.background
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

@ScreenPreview
@Composable
private fun Three() = PreviewScreen {
  ContentSuccess(
    modifier = Modifier.background(LocalTheme.current.pageBackground),
    budgets = persistentListOf(PreviewBudgetSynced, PreviewBudgetSynced, PreviewBudgetSynced),
    onClickOpen = {},
    onClickDelete = {},
  )
}
