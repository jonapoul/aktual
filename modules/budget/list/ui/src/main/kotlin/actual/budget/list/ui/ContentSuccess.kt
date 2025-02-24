package actual.budget.list.ui

import actual.budget.model.Budget
import actual.core.ui.LocalTheme
import actual.core.ui.PreviewScreen
import actual.core.ui.ScreenPreview
import actual.core.ui.Theme
import actual.core.ui.VerticalSpacer
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import my.nanihadesuka.compose.LazyColumnScrollbar
import my.nanihadesuka.compose.ScrollbarSettings

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
  LazyColumnScrollbar(
    state = listState,
    settings = ScrollbarSettings.Default,
  ) {
    LazyColumn(
      state = listState,
      modifier = modifier.fillMaxWidth(),
    ) {
      itemsIndexed(budgets) { index, budget ->
        BudgetListItem(
          budget = budget,
          theme = theme,
          onClickOpen = { onClickOpen(budget) },
          onClickDelete = { onClickDelete(budget) },
        )

        if (index < budgets.lastIndex) {
          VerticalSpacer()
        }
      }
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
