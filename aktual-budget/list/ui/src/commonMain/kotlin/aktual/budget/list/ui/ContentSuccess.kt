package aktual.budget.list.ui

import aktual.budget.model.Budget
import aktual.core.model.ColorSchemeType
import aktual.core.ui.BottomNavBarSpacing
import aktual.core.ui.BottomStatusBarSpacing
import aktual.core.ui.ColorSchemeParameters
import aktual.core.ui.Dimens
import aktual.core.ui.LocalTheme
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColorScheme
import aktual.core.ui.Theme
import aktual.core.ui.scrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
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
  val listState = rememberLazyListState()
  LazyColumn(
      modifier = modifier.scrollbar(listState),
      state = listState,
      verticalArrangement = Arrangement.spacedBy(Dimens.Medium),
  ) {
    items(budgets) { budget ->
      BudgetListItem(
          budget = budget,
          theme = theme,
          onClickOpen = { onClickOpen(budget) },
          onClickDelete = { onClickDelete(budget) },
      )
    }

    item {
      BottomStatusBarSpacing()
      BottomNavBarSpacing()
    }
  }
}

@PortraitPreview
@Composable
private fun PreviewContentSuccess(
    @PreviewParameter(ColorSchemeParameters::class) type: ColorSchemeType,
) =
    PreviewWithColorScheme(type) {
      ContentSuccess(
          modifier = Modifier.background(LocalTheme.current.pageBackground),
          budgets = persistentListOf(PreviewBudgetSynced, PreviewBudgetSynced, PreviewBudgetSynced),
          onClickOpen = {},
          onClickDelete = {},
      )
    }
