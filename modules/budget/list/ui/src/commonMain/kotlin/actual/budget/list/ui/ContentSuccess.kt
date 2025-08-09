package actual.budget.list.ui

import actual.budget.model.Budget
import actual.core.ui.LocalTheme
import actual.core.ui.Theme
import actual.core.ui.scrollbar
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList

@Stable
@Composable
internal fun ContentSuccess(
  budgets: ImmutableList<Budget>,
  onClickOpen: (Budget) -> Unit,
  onClickDelete: (Budget) -> Unit,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  val listState = rememberLazyListState()
  LazyColumn(
    modifier = modifier.scrollbar(listState),
    state = listState,
    verticalArrangement = Arrangement.spacedBy(4.dp),
  ) {
    itemsIndexed(budgets) { index, budget ->
      BudgetListItem(
        budget = budget,
        theme = theme,
        onClickOpen = { onClickOpen(budget) },
        onClickDelete = { onClickDelete(budget) },
      )
    }
  }
}
