package aktual.budget.reports.ui.montecarlo

import aktual.budget.reports.vm.McConfig
import aktual.budget.reports.vm.montecarlo.MonteCarloGraphView
import aktual.budget.reports.vm.montecarlo.MonteCarloResultsView
import aktual.budget.reports.vm.montecarlo.MonteCarloSection
import aktual.budget.reports.vm.montecarlo.RunPercentile
import androidx.compose.runtime.Immutable

internal sealed interface MonteCarloAction {
  data object NavBack : MonteCarloAction

  data object Save : MonteCarloAction

  data class Rename(val name: String) : MonteCarloAction

  data class Edit(val transform: (McConfig) -> McConfig) : MonteCarloAction

  data object AddPot : MonteCarloAction

  data object AddIncomeStream : MonteCarloAction

  data object AddContribution : MonteCarloAction

  data object AddSpendingPhase : MonteCarloAction

  data object AddTaxBand : MonteCarloAction

  data class SetKeepSurplus(val keep: Boolean) : MonteCarloAction

  data class SetShowTodaysMoney(val show: Boolean) : MonteCarloAction

  data class ToggleSection(val section: MonteCarloSection) : MonteCarloAction

  data class SetResultsView(val view: MonteCarloResultsView) : MonteCarloAction

  data class SetGraphView(val view: MonteCarloGraphView) : MonteCarloAction

  data class SetCashflowPercentile(val percentile: RunPercentile) : MonteCarloAction

  data class SelectRun(val index: Int?) : MonteCarloAction
}

@Immutable
internal fun interface MonteCarloActionHandler {
  operator fun invoke(action: MonteCarloAction)
}

internal fun MonteCarloActionHandler.edit(transform: (McConfig) -> McConfig) =
  invoke(MonteCarloAction.Edit(transform))
