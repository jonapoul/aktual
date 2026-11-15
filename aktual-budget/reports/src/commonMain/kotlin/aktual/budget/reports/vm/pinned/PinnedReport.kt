package aktual.budget.reports.vm.pinned

import aktual.budget.model.WidgetId
import aktual.budget.model.WidgetType
import aktual.budget.reports.vm.ChartData
import aktual.budget.reports.vm.dashboard.DashboardPage
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable data class ReportSummary(val id: WidgetId, val type: WidgetType, val name: String?)

// Data is null while the chart loads
@Immutable data class PinnedReport(val summary: ReportSummary, val data: ChartData?)

@Immutable data class ReportPage(val page: DashboardPage, val reports: ImmutableList<ReportSummary>)
