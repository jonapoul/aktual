package aktual.budget.list.ui

import aktual.budget.model.Budget
import aktual.budget.model.directoryId
import aktual.core.theme.Colors
import aktual.core.ui.BottomSpacing
import aktual.core.ui.ColoredParameters
import aktual.core.ui.Dimens
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColors
import aktual.core.ui.scrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Composable
internal fun ContentSuccess(
  budgets: ImmutableList<Budget>,
  onClickOpen: (Budget) -> Unit,
  onClickDelete: (Budget) -> Unit,
  modifier: Modifier = Modifier,
  contentPadding: PaddingValues = PaddingValues(),
  listState: LazyListState = rememberLazyListState(),
) {
  LazyColumn(
    modifier = modifier.scrollbar(listState),
    state = listState,
    contentPadding = contentPadding,
    verticalArrangement = Arrangement.spacedBy(Dimens.Medium),
  ) {
    items(budgets, key = { it.directoryId.value }) { budget ->
      BudgetListItem(
        modifier = Modifier.animateItem(),
        budget = budget,
        onClickOpen = { onClickOpen(budget) },
        onClickDelete = { onClickDelete(budget) },
      )
    }

    item { BottomSpacing() }
  }
}

@PortraitPreview
@Composable
private fun PreviewContentSuccess(@PreviewParameter(ColoredParameters::class) colors: Colors) =
  PreviewWithColors(colors) {
    ContentSuccess(
      modifier = Modifier.background(colors.pageBackground),
      budgets = persistentListOf(PreviewBudgetSynced, PreviewBudgetSynced, PreviewBudgetSynced),
      onClickOpen = {},
      onClickDelete = {},
    )
  }
