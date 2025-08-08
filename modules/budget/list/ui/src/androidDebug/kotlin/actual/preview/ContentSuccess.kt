package actual.preview

import actual.budget.list.ui.ContentSuccess
import actual.core.ui.LocalTheme
import actual.core.ui.PreviewThemedScreen
import actual.core.ui.TripleScreenPreview
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.persistentListOf

@TripleScreenPreview
@Composable
private fun Three() = PreviewThemedScreen {
  ContentSuccess(
    modifier = Modifier.background(LocalTheme.current.pageBackground),
    budgets = persistentListOf(PreviewBudgetSynced, PreviewBudgetSynced, PreviewBudgetSynced),
    onClickOpen = {},
    onClickDelete = {},
  )
}
