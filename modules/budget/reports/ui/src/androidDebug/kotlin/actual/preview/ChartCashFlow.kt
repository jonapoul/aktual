package actual.preview

import actual.budget.reports.ui.charts.CashFlowChart
import actual.budget.reports.ui.charts.PreviewCashFlow
import actual.budget.reports.ui.charts.PreviewShared.WIDTH
import actual.core.ui.CardShape
import actual.core.ui.LocalTheme
import actual.core.ui.PreviewThemedColumn
import actual.core.ui.PreviewThemedScreen
import actual.core.ui.TripleScreenPreview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@TripleScreenPreview
@Composable
private fun PreviewRegular() = PreviewThemedScreen(isPrivacyEnabled = false) {
  CashFlowChart(
    modifier = Modifier.padding(horizontal = 8.dp),
    data = PreviewCashFlow.DATA,
    compact = false,
  )
}

@TripleScreenPreview
@Composable
private fun PreviewRegularPrivate() = PreviewThemedScreen(isPrivacyEnabled = true) {
  CashFlowChart(
    modifier = Modifier.padding(horizontal = 8.dp),
    data = PreviewCashFlow.DATA,
    compact = false,
  )
}

@Preview(widthDp = WIDTH)
@Composable
private fun PreviewCompact() = PreviewThemedColumn(isPrivacyEnabled = false) {
  CashFlowChart(
    modifier = Modifier
      .background(LocalTheme.current.tableBackground, CardShape)
      .width(WIDTH.dp)
      .padding(5.dp),
    data = PreviewCashFlow.DATA,
    compact = true,
  )
}
