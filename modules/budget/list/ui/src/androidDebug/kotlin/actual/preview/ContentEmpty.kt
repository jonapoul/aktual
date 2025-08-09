package actual.preview

import actual.budget.list.ui.ContentEmpty
import actual.core.ui.PreviewThemedScreen
import actual.core.ui.TripleScreenPreview
import androidx.compose.runtime.Composable

@TripleScreenPreview
@Composable
private fun Empty() = PreviewThemedScreen {
  ContentEmpty(
    onCreateBudgetInBrowser = {},
  )
}
