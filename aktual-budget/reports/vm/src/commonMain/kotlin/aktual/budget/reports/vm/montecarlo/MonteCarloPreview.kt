package aktual.budget.reports.vm.montecarlo

import aktual.budget.reports.vm.McConfig
import kotlinx.collections.immutable.persistentListOf

// Simulates the plan synchronously, for UI previews and tests
fun previewMonteCarloState(
  config: McConfig = McConfig(),
  resultsView: MonteCarloResultsView = MonteCarloResultsView.Chart,
  graphView: MonteCarloGraphView = MonteCarloGraphView.All,
  selectedRun: RunPercentile? = null,
): MonteCarloState.Loaded {
  val simulation = simulate(config, deflate = true)
  val detailRun =
    when (resultsView) {
      MonteCarloResultsView.Cashflow -> simulation.runAt(RunPercentile.Median)
      MonteCarloResultsView.Runs -> selectedRun?.let(simulation::runAt)
      MonteCarloResultsView.Chart -> null
    }
  return MonteCarloState.Loaded(
    title = null,
    config = config,
    accounts = persistentListOf(),
    hasChanges = false,
    showTodaysMoney = true,
    resultsView = resultsView,
    graphView = graphView,
    cashflowPercentile = RunPercentile.Median,
    results = simulation.results,
    selectedRun = if (resultsView == MonteCarloResultsView.Runs) detailRun else null,
    runDetail = detailRun?.let { capture(simulation, it) },
  )
}
