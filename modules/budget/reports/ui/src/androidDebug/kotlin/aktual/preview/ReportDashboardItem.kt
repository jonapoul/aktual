/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.preview

import aktual.budget.reports.ui.ReportDashboardItem
import aktual.core.ui.PreviewThemedColumn
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
