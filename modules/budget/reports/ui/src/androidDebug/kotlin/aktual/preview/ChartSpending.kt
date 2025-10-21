/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.preview

import aktual.budget.reports.ui.charts.PreviewShared.WIDTH
import aktual.budget.reports.ui.charts.PreviewSpending
import aktual.budget.reports.ui.charts.SpendingChart
import aktual.core.ui.CardShape
import aktual.core.ui.LocalTheme
import aktual.core.ui.PreviewThemedColumn
import aktual.core.ui.PreviewThemedScreen
import aktual.core.ui.TripleScreenPreview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@TripleScreenPreview
@Composable
private fun PreviewRegular() = PreviewThemedScreen {
  Surface(
    modifier = Modifier
      .fillMaxSize()
      .background(LocalTheme.current.tableBackground),
  ) {
    SpendingChart(
      modifier = Modifier.padding(4.dp),
      data = PreviewSpending.JUL_2025,
      compact = false,
    )
  }
}

@Preview(widthDp = WIDTH)
@Composable
private fun PreviewCompact() = PreviewThemedColumn {
  SpendingChart(
    modifier = Modifier
      .background(LocalTheme.current.tableBackground, CardShape)
      .width(WIDTH.dp)
      .padding(5.dp),
    data = PreviewSpending.JUL_2025,
    compact = true,
  )
}
