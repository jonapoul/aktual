package actual.preview

import actual.budget.sync.ui.Content
import actual.core.model.Password
import actual.core.ui.PreviewThemedColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Preview
@Composable
private fun PreviewEmpty() = PreviewThemedColumn {
  Content(
    input = Password.Empty,
    onAction = {},
  )
}

@Preview
@Composable
private fun PreviewFull() = PreviewThemedColumn {
  Content(
    input = Password("abc-123"),
    onAction = {},
  )
}
