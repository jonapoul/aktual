package actual.preview

import actual.budget.reports.ui.charts.NetWorthChart
import actual.budget.reports.ui.charts.PreviewNetWorth
import actual.budget.reports.ui.charts.PreviewShared.WIDTH
import actual.core.ui.CardShape
import actual.core.ui.LocalTheme
import actual.core.ui.PreviewThemedColumn
import actual.core.ui.PreviewThemedScreen
import actual.core.ui.TripleScreenPreview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@TripleScreenPreview
@Composable
private fun MixedRegular() = PreviewThemedScreen {
  NetWorthChart(
    data = PreviewNetWorth.DATA,
    compact = false,
  )
}

@TripleScreenPreview
@Composable
private fun MixedRegularPrivate() = PreviewThemedScreen(isPrivacyEnabled = true) {
  NetWorthChart(
    data = PreviewNetWorth.DATA,
    compact = false,
  )
}

@Preview(widthDp = WIDTH)
@Composable
private fun PreviewCompact() = PreviewThemedColumn(isPrivacyEnabled = false) {
  NetWorthChart(
    modifier = Modifier
      .background(LocalTheme.current.tableBackground, CardShape)
      .width(WIDTH.dp),
    data = PreviewNetWorth.DATA,
    compact = true,
  )
}

@Preview(widthDp = WIDTH)
@Composable
private fun PreviewCompactPrivate() = PreviewThemedColumn(isPrivacyEnabled = true) {
  NetWorthChart(
    modifier = Modifier
      .background(LocalTheme.current.tableBackground, CardShape)
      .width(WIDTH.dp),
    data = PreviewNetWorth.DATA,
    compact = true,
  )
}
