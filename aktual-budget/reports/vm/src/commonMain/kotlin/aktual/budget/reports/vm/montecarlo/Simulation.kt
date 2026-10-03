package aktual.budget.reports.vm.montecarlo

import aktual.budget.model.Amount
import aktual.budget.reports.vm.McConfig
import aktual.budget.reports.vm.McResult
import aktual.budget.reports.vm.rankSimulationsWorstFirst
import aktual.budget.reports.vm.roundJs
import aktual.budget.reports.vm.runMonteCarlo
import aktual.core.model.Percent
import kotlinx.collections.immutable.toImmutableList

// One run of the whole simulation, with the worst-first ranking shared by the runs table and the
// cashflow view's scenario picker
@Suppress("UseDataClass")
internal class Simulation(
  val config: McConfig,
  val deflate: Boolean,
  val ranked: IntArray,
  val results: MonteCarloResults,
) {
  fun runAt(percentile: RunPercentile): Int =
    ranked[roundJs(percentile.fraction * (ranked.size - 1)).toInt()]
}

internal fun simulate(config: McConfig, deflate: Boolean): Simulation {
  val result = runMonteCarlo(config, deflate = deflate)
  val ranked = rankSimulationsWorstFirst(result.endingBalances, result.depletionYearBySimulation)
  return Simulation(config, deflate, ranked, result.toResults(config, ranked))
}

private fun McResult.toResults(config: McConfig, ranked: IntArray): MonteCarloResults {
  // Ages are of the year that couldn't be funded, matching the drill-in's failure row
  fun depletionAge(year: Int) = config.currentAge + year - 1

  return MonteCarloResults(
    successRate = Percent(roundJs(successRate * PERCENT_TENTHS) / TENTHS),
    depletionChance = Percent(roundJs((1 - successRate) * PERCENT_TENTHS) / TENTHS),
    medianEndingBalance = Amount(medianEndingBalance),
    medianTotalWithdrawn = Amount(medianTotalWithdrawn),
    medianDepletionAge = medianDepletionYear?.let(::depletionAge),
    earliestDepletionAge = earliestDepletionYear?.let(::depletionAge),
    latestDepletionAge = latestDepletionYear?.let(::depletionAge),
    simulationCount = simulationCount,
    currentAge = config.currentAge,
    endAge = config.currentAge + horizonYears,
    bands =
      percentileBands
        .map { band ->
          MonteCarloFanBand(
            age = config.currentAge + band.year,
            p5 = Amount(band.p5),
            p10 = Amount(band.p10),
            p25 = Amount(band.p25),
            p30 = Amount(band.p30),
            p50 = Amount(band.p50),
            p70 = Amount(band.p70),
            p75 = Amount(band.p75),
            p90 = Amount(band.p90),
            worstRun = Amount(worstRunPath[band.year]),
          )
        }
        .toImmutableList(),
    depletions =
      depletionCounts
        .mapIndexed { i, count -> MonteCarloDepletion(age = depletionAge(i + 1), count = count) }
        .toImmutableList(),
    failedCount = depletionCounts.sum(),
    runs =
      ranked
        .map { index ->
          MonteCarloRun(
            index = index,
            depletionAge =
              depletionYearBySimulation[index].takeIf { it != -1 }?.let(::depletionAge),
            endingBalance = Amount(roundJs(endingBalances[index])),
            totalWithdrawn = Amount(roundJs(totalWithdrawnBySimulation[index])),
          )
        }
        .toImmutableList(),
  )
}

private const val PERCENT_TENTHS = 1000
private const val TENTHS = 10.0
