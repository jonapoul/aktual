package aktual.budget.reports.vm

import assertk.assertThat
import assertk.assertions.isCloseTo
import assertk.assertions.isEqualTo
import assertk.assertions.isGreaterThan
import assertk.assertions.isLessThan
import assertk.assertions.isTrue
import kotlin.math.roundToInt
import kotlin.test.Test
import kotlinx.collections.immutable.persistentListOf

// Ported from upstream's "block bootstrap model" tests in monteCarloSimulation.test.ts
class MonteCarloBlockBootstrapTest {
  @Test
  fun `Continues to the next year about as often as the block length implies`() {
    val typical = continuationShare(blockLength = 10)
    assertThat(typical).isGreaterThan(0.85)
    assertThat(typical).isLessThan(0.95)
    assertThat(continuationShare(blockLength = 1)).isLessThan(0.12)
    assertThat(continuationShare(blockLength = 30)).isGreaterThan(0.94)
  }

  @Test
  fun `Wraps from the last historical year back to the first`() {
    val years = historyIndices(blockLength = 30, captureRunDetail = 0)
    val hasWrapped =
      (1 until years.size).any { years[it - 1] == ENCODED_HISTORY.lastIndex && years[it] == 0 }
    assertThat(hasWrapped).isTrue()
  }

  @Test
  fun `Pairs each year with its own historical year's return`() {
    val rows = run(blockLength = 10, captureRunDetail = 0).runDetail.orEmpty()
    for (row in rows) {
      val index = historyIndex(row)
      assertThat(checkNotNull(row.potReturns.first()))
        .isCloseTo(ENCODED_HISTORY[index].stocks, TOLERANCE)
    }
  }

  @Test
  fun `Runs the requested number of scenarios`() {
    val config = McConfig(returnModel = HistoricalBlockBootstrap, simulationCount = 1_500)
    assertThat(runMonteCarlo(config, history = ENCODED_HISTORY).simulationCount).isEqualTo(1_500)
  }

  @Test
  fun `Matches deterministic growth on a one-year history`() {
    val history = listOf(HistoricalReturn(2000, stocks = 0.05, bonds = 0.02, cash = 0.01, 0.0))
    val spending = persistentListOf(McSpendingPhase(annualWithdrawal = 300_000.0))
    val blocks =
      runMonteCarlo(
        McConfig(
          returnModel = HistoricalBlockBootstrap,
          pots =
            persistentListOf(
              McPot(id = "a", startingBalance = 10_000_000.0, allocationPreset = Equity100),
            ),
          spendingPhases = spending,
        ),
        history = history,
      )
    val deterministic =
      runMonteCarlo(
        McConfig(
          pots =
            persistentListOf(
              McPot(
                id = "a",
                startingBalance = 10_000_000.0,
                allocationPreset = Custom,
                expectedReturnMean = 0.05,
                returnStdDev = 0.0,
              ),
            ),
          spendingPhases = spending,
          inflationMean = 0.0,
          inflationStdDev = 0.0,
        ),
      )
    assertThat(blocks.percentileBands).isEqualTo(deterministic.percentileBands)
  }

  @Test
  fun `A captured run replays the same scenario`() {
    val config =
      McConfig(
        returnModel = HistoricalBlockBootstrap,
        spendingPhases = persistentListOf(McSpendingPhase(annualWithdrawal = 4_000_000.0)),
      )
    val first = runMonteCarlo(config)
    val captured = runMonteCarlo(config, captureRunDetail = 7)
    assertThat(captured.endingBalances.toList()).isEqualTo(first.endingBalances.toList())
  }

  private fun run(blockLength: Int, captureRunDetail: Int) =
    runMonteCarlo(
      McConfig(
        returnModel = HistoricalBlockBootstrap,
        historicalBlockLength = blockLength,
        pots =
          persistentListOf(
            McPot(id = "a", startingBalance = 100_000_000.0, allocationPreset = Equity100),
          ),
        spendingPhases = persistentListOf(McSpendingPhase(annualWithdrawal = 0.0)),
        currentAge = 20,
        targetAge = 120,
      ),
      history = ENCODED_HISTORY,
      deflate = false,
      captureRunDetail = captureRunDetail,
    )

  // The inflation rate encodes which historical year a simulated year replayed
  private fun historyIndex(row: McRunDetailRow): Int =
    (checkNotNull(row.inflation) / INFLATION_STEP).roundToInt() - 1

  private fun historyIndices(blockLength: Int, captureRunDetail: Int): List<Int> =
    run(blockLength, captureRunDetail).runDetail.orEmpty().map(::historyIndex)

  // The share of year-to-year steps that carried on to the next historical year, pooled over a few
  // captured runs
  private fun continuationShare(blockLength: Int): Double {
    var continued = 0
    var steps = 0
    repeat(RUNS) { runIndex ->
      val years = historyIndices(blockLength, runIndex)
      for (position in 1 until years.size) {
        steps++
        if (years[position] == (years[position - 1] + 1) % ENCODED_HISTORY.size) continued++
      }
    }
    return continued.toDouble() / steps
  }

  private companion object {
    const val RUNS = 5
    const val INFLATION_STEP = 0.0005
    const val TOLERANCE = 1e-9
    val ENCODED_HISTORY =
      List(20) { i ->
        HistoricalReturn(
          year = 2000 + i,
          stocks = i * 0.001,
          bonds = 0.0,
          cash = 0.0,
          inflation = (i + 1) * INFLATION_STEP,
        )
      }
  }
}
