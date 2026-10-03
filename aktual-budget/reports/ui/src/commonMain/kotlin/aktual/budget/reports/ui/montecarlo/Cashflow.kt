package aktual.budget.reports.ui.montecarlo

import aktual.budget.reports.vm.McConfig
import aktual.budget.reports.vm.montecarlo.CashflowChart
import aktual.budget.reports.vm.montecarlo.CashflowSeries
import aktual.budget.reports.vm.montecarlo.CashflowYear
import aktual.budget.reports.vm.montecarlo.MonteCarloState
import aktual.core.l10n.Strings
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.CardShape
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList

@Composable
internal fun CashflowView(state: MonteCarloState.Loaded, onAction: MonteCarloActionHandler) {
  RunPercentilePicker(
    value = state.cashflowPercentile,
    onValueChange = { onAction(MonteCarloAction.SetCashflowPercentile(it)) },
  )
  BodyText(Strings.monteCarloCashflowDesc)
  state.runDetail?.let { detail -> CashflowGraph(chart = detail.cashflow, config = state.config) }
}

// MonteCarloCashflowGraph plus its tooltip and legend. The tooltip is a panel under the chart,
// filled in by tapping a year
@Composable
internal fun CashflowGraph(chart: CashflowChart, config: McConfig, modifier: Modifier = Modifier) =
  Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
    var selectedAge by remember(chart) { mutableStateOf<Int?>(null) }

    CashflowBars(
      modifier = Modifier.fillMaxWidth().height(CHART_HEIGHT.dp),
      chart = chart,
      onSelectAge = { selectedAge = it },
    )

    val year = selectedAge?.let { age -> chart.years.firstOrNull { it.age == age } }
    if (year != null) {
      YearBreakdown(chart, config, year)
    }

    FlowRow(
      horizontalArrangement = Arrangement.spacedBy(40.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      LegendGroup(Strings.monteCarloCashflowMoneyIn, chart.inflows, config)
      LegendGroup(Strings.monteCarloCashflowMoneyOut, chart.outflows, config)
    }
  }

// MonteCarloCashflowGraphTooltip: only series with money moving this year, and only groups with any
@Composable
private fun YearBreakdown(
  chart: CashflowChart,
  config: McConfig,
  year: CashflowYear,
) =
  Column(
    modifier = Modifier.fillMaxWidth().background(colors.menuBackground, CardShape).padding(10.dp),
    verticalArrangement = Arrangement.spacedBy(4.dp),
  ) {
    val textColor = colors.menuItemText
    Text(
      text = Strings.monteCarloAge(year.age),
      style = typography.bodyMedium,
      fontWeight = FontWeight.Bold,
      color = textColor,
    )
    if (year.afterDepletion) {
      Text(
        text = Strings.monteCarloCashflowAfterDepletion,
        style = typography.bodySmall,
        color = colors.errorText,
      )
    }
    if (year.unspentIncome > 0) {
      Text(
        text = Strings.monteCarloCashflowUnspent(year.unspentIncome.money()),
        style = typography.bodySmall,
        color = textColor,
      )
    }

    val series = chart.series
    for (group in chart.groups) {
      val members = group.series.filter { year.amounts[it] != 0L }
      if (members.isEmpty()) continue
      val total = members.sumOf { year.amounts[it] }
      BreakdownRow(
        label = group.kind.string(),
        amount = total.money(),
        color =
          if (group.listMembers) null else seriesColor(series[members.first()].colorIndex, colors),
        isBold = true,
        textColor = textColor,
      )
      if (group.listMembers) {
        for (member in members) {
          BreakdownRow(
            modifier = Modifier.padding(start = 16.dp),
            label = series[member].label(config),
            amount = year.amounts[member].money(),
            color = seriesColor(series[member].colorIndex, colors),
            isBold = false,
            textColor = textColor,
          )
        }
      }
    }
  }

@Composable
private fun BreakdownRow(
  label: String,
  amount: String,
  color: Color?,
  isBold: Boolean,
  textColor: Color,
  modifier: Modifier = Modifier,
) =
  Row(
    modifier = modifier.fillMaxWidth(),
    verticalAlignment = CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(6.dp),
  ) {
    color?.let { Swatch(it) }
    val weight = if (isBold) FontWeight.Bold else FontWeight.Normal
    Text(
      modifier = Modifier.weight(1f),
      text = label,
      style = typography.bodySmall,
      fontWeight = weight,
      color = textColor,
    )
    Text(text = amount, style = typography.bodySmall, fontWeight = weight, color = textColor)
  }

@Composable
private fun LegendGroup(heading: String, series: ImmutableList<CashflowSeries>, config: McConfig) =
  Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
    GroupHeading(heading)
    FlowRow(
      horizontalArrangement = Arrangement.spacedBy(15.dp),
      verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      for (entry in series) {
        Row(
          verticalAlignment = CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          Swatch(seriesColor(entry.colorIndex, colors), size = 12.dp)
          Text(text = entry.label(config), style = typography.bodySmall, color = colors.pageText)
        }
      }
    }
  }

@Composable
private fun Swatch(color: Color, size: Dp = 10.dp) =
  Box(modifier = Modifier.size(size).background(color, SwatchShape))

private val SwatchShape = RoundedCornerShape(2.dp)

private const val CHART_HEIGHT = 320
