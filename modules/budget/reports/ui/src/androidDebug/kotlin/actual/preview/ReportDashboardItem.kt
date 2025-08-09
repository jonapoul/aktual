package actual.preview

import actual.budget.reports.ui.ReportDashboardItem
import actual.core.ui.PreviewThemedColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Preview
@Composable
private fun PreviewItem1() = PreviewThemedColumn {
  ReportDashboardItem(
    item = ITEM_1,
    onAction = {},
  )
}

@Preview
@Composable
private fun PreviewItem2() = PreviewThemedColumn {
  ReportDashboardItem(
    item = ITEM_2,
    onAction = {},
  )
}

@Preview
@Composable
private fun PreviewItem3() = PreviewThemedColumn {
  ReportDashboardItem(
    item = ITEM_3,
    onAction = {},
  )
}
