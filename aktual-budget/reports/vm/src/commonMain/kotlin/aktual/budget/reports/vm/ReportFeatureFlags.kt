package aktual.budget.reports.vm

import aktual.budget.model.SyncedPrefKey.FeatureFlag
import aktual.budget.model.WidgetType
import aktual.prefs.BudgetFeatureFlags
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

// The experimental flag a report type needs, like upstream's Overview.tsx
internal val WidgetType.featureFlag: FeatureFlag?
  get() =
    when (this) {
      Sankey -> SankeyReport
      BalanceForecast -> BalanceForecastReport
      BudgetAnalysis -> BudgetAnalysisReport
      MonteCarlo -> MonteCarloReport
      Formula -> FormulaMode
      NetWorth,
      CashFlow,
      Spending,
      Custom,
      Markdown,
      Summary,
      Calendar,
      Crossover,
      AgeOfMoney,
      Unknown -> null
    }

internal fun BudgetFeatureFlags.enabledTypes(): Flow<List<WidgetType>> {
  val gated = WidgetType.known.mapNotNull { it.featureFlag }.distinct()
  return combine(gated.map { this[it].asFlow() }) { values ->
    val enabled = gated.filterIndexed { i, _ -> values[i] }.toSet()
    WidgetType.known.filter { type -> type.featureFlag?.let { it in enabled } ?: true }
  }
}
