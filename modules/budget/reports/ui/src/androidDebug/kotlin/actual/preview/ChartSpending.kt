package actual.preview

import actual.budget.reports.ui.charts.PreviewShared.WIDTH
import actual.budget.reports.ui.charts.PreviewSpending
import actual.budget.reports.ui.charts.SpendingChart
import actual.core.ui.CardShape
import actual.core.ui.LocalTheme
import actual.core.ui.PreviewThemedColumn
import actual.core.ui.PreviewThemedScreen
import actual.core.ui.TripleScreenPreview
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
