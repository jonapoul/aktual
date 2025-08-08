package actual.preview

import actual.budget.transactions.ui.DateHeader
import actual.budget.transactions.ui.StateSource
import actual.core.ui.PreviewThemedColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Preview
@Composable
private fun PreviewDateHeader() = PreviewThemedColumn {
  DateHeader(
    date = PREVIEW_DATE,
    source = StateSource.Empty,
    onAction = {},
  )
}
