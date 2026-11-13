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
import aktual.core.ui.OnAction
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier

/** When [compact], keep in sync with [ShimmerChart] */
@Composable
fun ReportChart(
  data: ChartData,
  compact: Boolean,
  onAction: OnAction<ChartAction>,
  modifier: Modifier = Modifier,
  includeHeader: Boolean = true,
  zoom: ChartZoomState = rememberChartZoomState(data),
) =
  CompositionLocalProvider(LocalCompactChart provides compact) {
    when (data) {
      is AgeOfMoneyData -> AgeOfMoneyChart(data, modifier, includeHeader, zoom)
      is BalanceForecastData -> BalanceForecastChart(data, modifier, includeHeader, zoom, onAction)
      is BudgetAnalysisData -> BudgetAnalysisChart(data, modifier, includeHeader, zoom)
      is CashFlowData -> CashFlowChart(data, modifier, includeHeader, zoom)
      is CrossoverData -> CrossoverChart(data, modifier, includeHeader, zoom)
      is MonteCarloData -> MonteCarloChart(data, modifier, includeHeader, zoom)
      is NetWorthData -> NetWorthChart(data, modifier, includeHeader, zoom)
      is SankeyData -> SankeyChart(data, modifier, includeHeader)
      is SummaryData -> SummaryChart(data, onAction, modifier, includeHeader)
      is CalendarData -> CalendarChart(data, onAction, modifier, includeHeader)
      is SpendingData -> SpendingChart(data, modifier, includeHeader, zoom)
      is FormulaData -> FormulaChart(data, modifier, includeHeader)
      is TextData -> TextChart(data, onAction, modifier)
      is CustomData -> CustomChart(data, modifier, includeHeader) { onAction(DrillDown(it)) }
      is UnsupportedData -> UnsupportedChart(data, modifier)
    }
  }
