package actual.preview

import actual.budget.list.ui.ContentLoading
import actual.core.ui.PreviewThemedScreen
import actual.core.ui.TripleScreenPreview
import androidx.compose.runtime.Composable

@TripleScreenPreview
@Composable
private fun Loading() = PreviewThemedScreen {
  ContentLoading()
}
