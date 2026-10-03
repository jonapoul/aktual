package aktual.budget.reports.ui.montecarlo

import aktual.budget.model.Amount
import aktual.budget.reports.vm.McConfig
import aktual.budget.reports.vm.McPot
import aktual.budget.reports.vm.montecarlo.CashflowGroupKind
import aktual.budget.reports.vm.montecarlo.CashflowSeries
import aktual.budget.reports.vm.montecarlo.MonteCarloGraphView
import aktual.budget.reports.vm.montecarlo.MonteCarloResultsView
import aktual.budget.reports.vm.montecarlo.RunPercentile
import aktual.budget.reports.vm.ordinaryOrdinal
import aktual.core.l10n.Strings
import aktual.core.ui.formattedString
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import kotlinx.collections.immutable.ImmutableList

// getMonteCarloPotLabel(): the pot's name, else "Surplus cash" or "Pot N" numbered among the
// ordinary pots
@Composable
internal fun potLabel(pots: ImmutableList<McPot>, index: Int): String {
  val pot = pots[index]
  return when {
    pot.name.isNotEmpty() -> pot.name
    pot.isSurplus -> Strings.monteCarloPotSurplus
    else -> Strings.monteCarloPotNumbered(pots.ordinaryOrdinal(index))
  }
}

@Composable
internal fun surplusPotLabel(pots: ImmutableList<McPot>): String {
  val index = pots.indexOfFirst { it.isSurplus }
  return if (index >= 0) potLabel(pots, index) else Strings.monteCarloPotSurplus
}

@Composable
internal fun CashflowSeries.label(config: McConfig): String =
  when (kind) {
    Pot -> potLabel(config.pots, index)
    Income -> name.ifEmpty { Strings.monteCarloIncomeNumbered(index + 1) }
    Phase -> name.ifEmpty { Strings.monteCarloPhaseNumbered(index + 1) }
    Tax -> Strings.monteCarloCashflowTax
    Contribution -> name.ifEmpty { Strings.monteCarloContributionNumbered(index + 1) }
    Surplus -> Strings.monteCarloCashflowSavedInto(surplusPotLabel(config.pots))
  }

@Composable
internal fun CashflowGroupKind.string(): String =
  when (this) {
    Withdrawals -> Strings.monteCarloCashflowWithdrawals
    Income -> Strings.monteCarloCashflowIncome
    Tax -> Strings.monteCarloCashflowTax
    Spending -> Strings.monteCarloCashflowSpending
    Contributions -> Strings.monteCarloCashflowContributions
    Saved -> Strings.monteCarloCashflowSaved
  }

@Composable
internal fun RunPercentile.string(): String =
  when (this) {
    Worst -> Strings.monteCarloPercentileWorst
    P25 -> Strings.monteCarloPercentile25
    Median -> Strings.monteCarloPercentileMedian
    P75 -> Strings.monteCarloPercentile75
    Best -> Strings.monteCarloPercentileBest
  }

@Composable
internal fun MonteCarloGraphView.string(): String =
  when (this) {
    All -> Strings.monteCarloGraphAll
    SingleWorst -> Strings.monteCarloGraphSingleWorst
    WorstCase -> Strings.monteCarloGraphWorstCase
    Pessimistic -> Strings.monteCarloGraphPessimistic
    Median -> Strings.monteCarloGraphMedian
    Optimistic -> Strings.monteCarloGraphOptimistic
  }

@Composable
internal fun MonteCarloResultsView.string(): String =
  when (this) {
    Chart -> Strings.monteCarloViewChart
    Cashflow -> Strings.monteCarloViewCashflow
    Runs -> Strings.monteCarloViewRuns
  }

@Composable @ReadOnlyComposable internal fun Long.money(): String = Amount(this).formattedString()

// formatRuleRate(): enough precision that the displayed rate reproduces the displayed amounts, with
// trailing zeros trimmed so simple rates read cleanly
internal fun formatRuleRate(rate: Double): String = "${trimmed(rate * PERCENT, places = 4)}%"

internal fun formatMultiple(multiple: Double): String = "${trimmed(multiple, places = 2)}×"

internal fun formatPercent(rate: Double, places: Int = 2): String =
  "%.${places}f%%".format(rate * PERCENT)

private fun trimmed(value: Double, places: Int): String =
  "%.${places}f".format(value).trimEnd('0').trimEnd('.', ',')

private const val PERCENT = 100.0
