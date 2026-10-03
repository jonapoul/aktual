package aktual.budget.reports.ui.montecarlo

import aktual.budget.reports.vm.McConfig
import aktual.budget.reports.vm.montecarlo.MonteCarloGraphView
import aktual.budget.reports.vm.montecarlo.MonteCarloResultsView
import aktual.budget.reports.vm.montecarlo.RunPercentile
import androidx.compose.runtime.Immutable

internal sealed interface MonteCarloAction {
  data object NavBack : MonteCarloAction

  data object Save : MonteCarloAction

  data class SetConfig(val config: McConfig) : MonteCarloAction

  data class SetShowTodaysMoney(val show: Boolean) : MonteCarloAction

  data class SetResultsView(val view: MonteCarloResultsView) : MonteCarloAction

  data class SetGraphView(val view: MonteCarloGraphView) : MonteCarloAction

  data class SetCashflowPercentile(val percentile: RunPercentile) : MonteCarloAction

  data class SelectRun(val index: Int?) : MonteCarloAction
}

@Immutable
internal fun interface MonteCarloActionHandler {
  operator fun invoke(action: MonteCarloAction)
}
