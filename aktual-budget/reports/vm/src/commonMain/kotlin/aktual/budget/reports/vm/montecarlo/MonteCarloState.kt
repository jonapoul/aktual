package aktual.budget.reports.vm.montecarlo

import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.reports.vm.McConfig
import aktual.budget.reports.vm.McRunDetailRow
import aktual.core.model.Percent
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable
sealed interface MonteCarloState {
  data object Loading : MonteCarloState

  data object NotFound : MonteCarloState

  data class Loaded(
    val title: String?,
    // The plan being edited, with linked pots taking their accounts' live balances
    val config: McConfig,
    val accounts: ImmutableList<MonteCarloAccount>,
    val hasChanges: Boolean,
    val showTodaysMoney: Boolean,
    val resultsView: MonteCarloResultsView,
    val graphView: MonteCarloGraphView,
    val cashflowPercentile: RunPercentile,
    // null while the simulation runs
    val results: MonteCarloResults?,
    // The run picked from the runs table, if any
    val selectedRun: Int?,
    // The cashflow view's run, or the selected run in the runs view
    val runDetail: MonteCarloRunDetail?,
  ) : MonteCarloState
}

data class MonteCarloAccount(
  val id: AccountId,
  val name: String,
  val isClosed: Boolean,
  val isOffBudget: Boolean,
)

enum class MonteCarloResultsView {
  Chart,
  Cashflow,
  Runs,
}

enum class MonteCarloGraphView {
  All,
  SingleWorst,
  WorstCase,
  Pessimistic,
  Median,
  Optimistic,
}

// Points along the worst-first ranking of runs, shared by the runs table's jumps and the cashflow
// view's scenario picker so both land on the same runs
enum class RunPercentile(val fraction: Double) {
  Worst(fraction = 0.0),
  P25(fraction = 0.25),
  Median(fraction = 0.5),
  P75(fraction = 0.75),
  Best(fraction = 1.0),
}

data class MonteCarloResults(
  val successRate: Percent,
  val depletionChance: Percent,
  val medianEndingBalance: Amount,
  val medianTotalWithdrawn: Amount,
  // Ages are of the year that couldn't be funded
  val medianDepletionAge: Int?,
  val earliestDepletionAge: Int?,
  val latestDepletionAge: Int?,
  val simulationCount: Int,
  val currentAge: Int,
  // The age the simulation runs to, which differs from the target age when the horizon is clamped
  val endAge: Int,
  val bands: ImmutableList<MonteCarloFanBand>,
  // One bar per simulated year
  val depletions: ImmutableList<MonteCarloDepletion>,
  val failedCount: Int,
  // Every run, worst outcome first
  val runs: ImmutableList<MonteCarloRun>,
)

data class MonteCarloFanBand(
  val age: Int,
  val p5: Amount,
  val p10: Amount,
  val p25: Amount,
  val p30: Amount,
  val p50: Amount,
  val p70: Amount,
  val p75: Amount,
  val p90: Amount,
  val worstRun: Amount,
)

data class MonteCarloDepletion(val age: Int, val count: Int)

data class MonteCarloRun(
  val index: Int,
  // null when the run survived
  val depletionAge: Int?,
  val endingBalance: Amount,
  val totalWithdrawn: Amount,
)

data class MonteCarloRunDetail(
  val index: Int,
  // Includes the unfunded years after a failure, which only the cashflow chart shows
  val rows: ImmutableList<McRunDetailRow>,
  val cashflow: CashflowChart,
)
