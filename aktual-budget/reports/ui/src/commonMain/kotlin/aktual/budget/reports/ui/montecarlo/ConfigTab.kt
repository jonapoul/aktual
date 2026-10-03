package aktual.budget.reports.ui.montecarlo

import aktual.core.l10n.Strings
import androidx.compose.runtime.Composable

internal enum class ConfigTab {
  Plan,
  Pots,
  Income,
  Contributions,
  Spending,
  Tax,
}

@Composable
internal fun ConfigTab.string(): String =
  when (this) {
    Plan -> Strings.monteCarloTabPlan
    Pots -> Strings.monteCarloTabPots
    Income -> Strings.monteCarloTabIncome
    Contributions -> Strings.monteCarloTabContributions
    Spending -> Strings.monteCarloTabSpending
    Tax -> Strings.monteCarloTabTax
  }

@Composable
internal fun ConfigTab.description(): String =
  when (this) {
    Plan -> Strings.monteCarloDescPlan
    Pots -> Strings.monteCarloDescPots
    Income -> Strings.monteCarloDescIncome
    Contributions -> Strings.monteCarloDescContributions
    Spending -> Strings.monteCarloDescSpending
    Tax -> Strings.monteCarloDescTax
  }

@Composable
internal fun ConfigTab.help(): String? =
  when (this) {
    Income -> Strings.monteCarloHelpIncome
    Contributions -> Strings.monteCarloHelpContributions
    Plan,
    Pots,
    Spending,
    Tax -> null
  }
