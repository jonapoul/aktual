package dev.jonpoulton.actual.listbudgets.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import dev.jonpoulton.actual.core.ui.ActualColorScheme
import dev.jonpoulton.actual.core.ui.ActualScreenPreview
import dev.jonpoulton.actual.core.ui.LocalActualColorScheme
import dev.jonpoulton.actual.core.ui.PreviewActualScreen
import dev.jonpoulton.actual.listbudgets.vm.Budget
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Stable
@Composable
internal fun ContentSuccess(
  budgets: ImmutableList<Budget>,
  onClickOpen: (Budget) -> Unit,
  onClickDelete: (Budget) -> Unit,
  modifier: Modifier = Modifier,
  colors: ActualColorScheme = LocalActualColorScheme.current,
) {
  LazyColumn(
    modifier = modifier.fillMaxWidth(),
  ) {
    items(budgets) { budget ->
      BudgetListItem(
        budget = budget,
        colors = colors,
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

