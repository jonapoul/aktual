package aktual.budget.reports.components

import aktual.budget.reports.components.ChartAction.DrillDown
import aktual.budget.reports.vm.AgeOfMoneyData
import aktual.budget.reports.vm.BalanceForecastData
import aktual.budget.reports.vm.BudgetAnalysisData
import aktual.budget.reports.vm.CalendarData
import aktual.budget.reports.vm.CashFlowData
import aktual.budget.reports.vm.ChartData
import aktual.budget.reports.vm.CrossoverData
import aktual.budget.reports.vm.CustomData
import aktual.budget.reports.vm.FormulaData
import aktual.budget.reports.vm.MonteCarloData
import aktual.budget.reports.vm.NetWorthData
import aktual.budget.reports.vm.SankeyData
import aktual.budget.reports.vm.SpendingData
import aktual.budget.reports.vm.SummaryData
import aktual.budget.reports.vm.TextData
import aktual.budget.reports.vm.UnsupportedData
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ReportChart(
  data: ChartData,
  compact: Boolean,
  onAction: ChartActionListener,
  modifier: Modifier = Modifier,
  includeHeader: Boolean = true,
  zoom: ChartZoomState = rememberChartZoomState(data),
) =
  when (data) {
    is AgeOfMoneyData -> AgeOfMoneyChart(data, compact, modifier, includeHeader, zoom)
    is BalanceForecastData -> BalanceForecastChart(data, compact, modifier, includeHeader, zoom)
    is BudgetAnalysisData -> BudgetAnalysisChart(data, compact, modifier, includeHeader, zoom)
    is CashFlowData -> CashFlowChart(data, compact, modifier, includeHeader, zoom)
    is CrossoverData -> CrossoverChart(data, compact, modifier, includeHeader, zoom)
    is MonteCarloData -> MonteCarloChart(data, compact, modifier, includeHeader, zoom)
    is NetWorthData -> NetWorthChart(data, compact, modifier, includeHeader, zoom)
    is SankeyData -> SankeyChart(data, compact, modifier, includeHeader)
    is SummaryData -> SummaryChart(data, compact, onAction, modifier, includeHeader)
    is CalendarData -> CalendarChart(data, compact, onAction, modifier, includeHeader)
    is SpendingData -> SpendingChart(data, compact, modifier, includeHeader, zoom)
    is FormulaData -> FormulaChart(data, compact, modifier, includeHeader)
    is TextData -> TextChart(data, compact, onAction, modifier)
    is CustomData -> CustomChart(data, compact, modifier, includeHeader) { onAction(DrillDown(it)) }
    is UnsupportedData -> UnsupportedChart(data, modifier)
  }
