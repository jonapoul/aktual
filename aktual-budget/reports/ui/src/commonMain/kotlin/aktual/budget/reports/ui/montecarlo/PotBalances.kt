package aktual.budget.reports.ui.montecarlo

import aktual.budget.reports.vm.McConfig
import aktual.budget.reports.vm.montecarlo.MonteCarloState
import aktual.budget.reports.vm.montecarlo.PotBalancesChart
import aktual.budget.reports.vm.montecarlo.PotBalancesYear
import aktual.core.l10n.Strings
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.CardShape
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun PotBalancesView(state: MonteCarloState.Loaded, onAction: MonteCarloActionHandler) {
  RunPercentilePicker(
    value = state.cashflowPercentile,
    onValueChange = { onAction(MonteCarloAction.SetCashflowPercentile(it)) },
  )
  BodyText(Strings.monteCarloPotBalancesDesc)
  state.runDetail?.let { detail ->
    PotBalancesGraph(chart = detail.potBalances, config = state.config)
  }
}

// MonteCarloPotBalancesGraph plus its tooltip and legend. The tooltip is a panel under the chart,
// filled in by tapping a year
@Composable
internal fun PotBalancesGraph(
  chart: PotBalancesChart,
  config: McConfig,
  modifier: Modifier = Modifier,
) =
  Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
    var selectedAge by remember(chart) { mutableStateOf<Int?>(null) }

    PotBalancesAreas(
      modifier = Modifier.fillMaxWidth().height(CHART_HEIGHT.dp),
      chart = chart,
      onSelectAge = { selectedAge = it },
    )

    val year = selectedAge?.let { age -> chart.years.firstOrNull { it.age == age } }
    if (year != null) {
      YearBalances(chart, config, year)
    }

    LegendGroup(Strings.monteCarloPotBalancesPots, chart.series, config)
  }

// MonteCarloCashflowGraphTooltip with showZeroMembers: every pot is listed each year under the
// total, since balances read better with nothing hidden
@Composable
private fun YearBalances(chart: PotBalancesChart, config: McConfig, year: PotBalancesYear) =
  Column(
    modifier = Modifier.fillMaxWidth().background(colors.menuBackground, CardShape).padding(10.dp),
    verticalArrangement = Arrangement.spacedBy(4.dp),
  ) {
    val textColor = colors.menuItemText
    Text(
      text = Strings.monteCarloAge(year.age),
      style = typography.bodyMedium,
      fontWeight = Bold,
      color = textColor,
    )
    if (year.afterDepletion) {
      Text(
        text = Strings.monteCarloPotBalancesAfterDepletion,
        style = typography.bodySmall,
        color = colors.errorText,
      )
    }
    BreakdownRow(
      label = Strings.monteCarloPotBalancesTotal,
      amount = year.total.money(),
      color = null,
      isBold = true,
      textColor = textColor,
    )
    chart.series.forEachIndexed { i, series ->
      BreakdownRow(
        modifier = Modifier.padding(start = 16.dp),
        label = series.label(config),
        amount = year.balances.getOrElse(i) { 0 }.money(),
        color = seriesColor(series.colorIndex, colors),
        isBold = false,
        textColor = textColor,
      )
    }
  }

private const val CHART_HEIGHT = 320
