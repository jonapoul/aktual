package aktual.budget.reports.vm.montecarlo

import aktual.budget.reports.vm.McConfig
import aktual.budget.reports.vm.McRunDetailRow
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

// Ported from
// packages/desktop-client/src/components/reports/graphs/util/monteCarloPotBalancesChart.ts

@Immutable
data class PotBalancesYear(
  val year: Int,
  val age: Int,
  // A synthetic year after the plan ran out, with every pot empty
  val afterDepletion: Boolean,
  // Each pot's end-of-year balance, in configured pot order
  val balances: ImmutableList<Long>,
) {
  val total: Long
    get() = balances.sum()
}

@Immutable
data class PotBalancesChart(
  // One stacked series per pot, coloured as in the cashflow chart
  val series: ImmutableList<CashflowSeries>,
  val years: ImmutableList<PotBalancesYear>,
)

// buildMonteCarloPotBalancesChart(): what every pot was worth at the end of each year of one run.
// Every pot gets a series even when it stays empty, so the legend matches the pots list
fun buildPotBalancesChart(
  rows: List<McRunDetailRow>,
  config: McConfig,
  paletteSize: Int,
): PotBalancesChart {
  val pots = config.pots
  val series = pots.mapIndexed { i, pot -> CashflowSeries(Pot, i, pot.name, i % paletteSize) }
  val years = rows.map { row ->
    PotBalancesYear(
      year = row.year,
      age = config.currentAge + row.year - 1,
      afterDepletion = row.afterDepletion,
      balances = pots.indices.map { row.potBalances.getOrElse(it) { 0 } }.toImmutableList(),
    )
  }
  return PotBalancesChart(series = series.toImmutableList(), years = years.toImmutableList())
}
