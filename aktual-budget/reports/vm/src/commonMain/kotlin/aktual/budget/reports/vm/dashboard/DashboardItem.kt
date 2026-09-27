package aktual.budget.reports.vm.dashboard

import aktual.budget.model.WidgetId
import aktual.budget.reports.vm.AgeOfMoneyReportMeta
import aktual.budget.reports.vm.BalanceForecastReportMeta
import aktual.budget.reports.vm.BudgetAnalysisReportMeta
import aktual.budget.reports.vm.CalendarReportMeta
import aktual.budget.reports.vm.CashFlowReportMeta
import aktual.budget.reports.vm.CrossoverReportMeta
import aktual.budget.reports.vm.CustomReportMeta
import aktual.budget.reports.vm.FormulaReportMeta
import aktual.budget.reports.vm.MarkdownReportMeta
import aktual.budget.reports.vm.MonteCarloReportMeta
import aktual.budget.reports.vm.NetWorthReportMeta
import aktual.budget.reports.vm.ReportMeta
import aktual.budget.reports.vm.SankeyReportMeta
import aktual.budget.reports.vm.SpendingReportMeta
import aktual.budget.reports.vm.SummaryReportMeta
import aktual.budget.reports.vm.UnsupportedReportMeta
import androidx.compose.runtime.Immutable

@Immutable
data class DashboardItem(
  val id: WidgetId,
  val x: Int,
  val y: Int,
  val meta: ReportMeta,
  val width: Int = 4,
  val height: Int = 2,
)

val DashboardItem.isRenamable: Boolean
  get() = meta !is MarkdownReportMeta && meta !is UnsupportedReportMeta

// Custom reports keep their name in a separate table, so it's not available here
val DashboardItem.name: String?
  get() =
    when (val meta = meta) {
      is AgeOfMoneyReportMeta -> meta.name
      is BalanceForecastReportMeta -> meta.name
      is BudgetAnalysisReportMeta -> meta.name
      is CalendarReportMeta -> meta.name
      is CashFlowReportMeta -> meta.name
      is CrossoverReportMeta -> meta.name
      is FormulaReportMeta -> meta.name
      is MonteCarloReportMeta -> meta.name
      is NetWorthReportMeta -> meta.name
      is SankeyReportMeta -> meta.name
      is SpendingReportMeta -> meta.name
      is SummaryReportMeta -> meta.name
      is CustomReportMeta,
      is MarkdownReportMeta,
      is UnsupportedReportMeta -> null
    }
