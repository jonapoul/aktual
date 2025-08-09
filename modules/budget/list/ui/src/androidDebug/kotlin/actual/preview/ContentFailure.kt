package actual.preview

import actual.budget.list.ui.ContentFailure
import actual.core.ui.PreviewThemedScreen
import actual.core.ui.TripleScreenPreview
import androidx.compose.runtime.Composable

@TripleScreenPreview
@Composable
private fun Failure() = PreviewThemedScreen {
  ContentFailure(
    reason = "Failed to do the thing, here's a bit more text to show how it behaves when wrapping",
    onClickRetry = {},
  )
}

@TripleScreenPreview
@Composable
private fun NoReason() = PreviewThemedScreen {
  ContentFailure(
    reason = null,
    onClickRetry = {},
  )
}
