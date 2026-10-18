package aktual.budget.reports.ui.pinned

import aktual.budget.reports.ui.charts.ReportChart
import aktual.budget.reports.ui.dashboard.ChartHeight
import aktual.budget.reports.ui.dashboard.ChartPadding
import aktual.budget.reports.ui.dashboard.ShimmerDashboardItem
import aktual.budget.reports.ui.string
import aktual.budget.reports.vm.ChartData
import aktual.budget.reports.vm.pinned.ReportSummary
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

// A report as the dashboard draws it, for showing elsewhere. Shimmers until [data] loads
@Composable
fun PinnedReportChart(data: ChartData?, modifier: Modifier = Modifier) {
  if (data == null) {
    ShimmerDashboardItem(modifier = modifier)
  } else {
    ReportChart(
      modifier = modifier.fillMaxWidth().padding(ChartPadding).height(ChartHeight),
      data = data,
      compact = true,
      onAction = {},
    )
  }
}

// Falls back to the report type for unnamed reports
@Composable
fun ReportSummary.displayName(): String = name?.takeIf { it.isNotBlank() } ?: type.string()
