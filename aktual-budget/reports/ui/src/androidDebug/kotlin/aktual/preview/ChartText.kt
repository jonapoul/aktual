/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.preview

import aktual.budget.reports.ui.charts.PreviewShared.WIDTH
import aktual.budget.reports.ui.charts.PreviewText
import aktual.budget.reports.ui.charts.TextChart
import aktual.core.ui.CardShape
import aktual.core.ui.LocalTheme
import aktual.core.ui.PreviewThemedColumn
import aktual.core.ui.PreviewThemedScreen
import aktual.core.ui.TripleScreenPreview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@TripleScreenPreview
@Composable
private fun PreviewRegular() = PreviewThemedScreen(isPrivacyEnabled = false) {
  Surface(Modifier.background(LocalTheme.current.tableBackground)) {
    TextChart(
      modifier = Modifier
        .background(LocalTheme.current.tableBackground, CardShape)
        .padding(horizontal = 4.dp),
      data = PreviewText.DATA,
      compact = false,
      onAction = {},
    )
  }
}

@TripleScreenPreview
@Composable
private fun PreviewRegularPrivate() = PreviewThemedScreen(isPrivacyEnabled = true) {
  Surface(Modifier.background(LocalTheme.current.tableBackground)) {
    TextChart(
      modifier = Modifier
        .background(LocalTheme.current.tableBackground, CardShape)
        .padding(horizontal = 4.dp),
      data = PreviewText.DATA,
      compact = false,
      onAction = {},
    )
  }
}

@Preview(widthDp = WIDTH)
@Composable
private fun PreviewCompact() = PreviewThemedColumn(isPrivacyEnabled = false) {
  TextChart(
    modifier = Modifier
      .background(LocalTheme.current.tableBackground, CardShape)
      .width(WIDTH.dp)
      .height(300.dp)
      .padding(4.dp),
    data = PreviewText.DATA,
    compact = true,
    onAction = {},
  )
}
