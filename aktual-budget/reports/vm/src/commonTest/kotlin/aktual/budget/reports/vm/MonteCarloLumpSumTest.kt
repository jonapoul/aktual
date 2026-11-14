package aktual.budget.reports.vm

import assertk.all
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import assertk.assertions.isZero
import assertk.assertions.prop
import kotlin.test.Test
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

class MonteCarloLumpSumTest {
  @Test
  fun `Grosses a lump sum up for its own tax in its year`() {
    val rows = run(lumpSum = McLumpSum(id = "l", age = 61, amount = 1_000_000.0, taxRate = 0.2))
    assertThat(rows[0].lumpSumWithdrawal).isZero()
    assertThat(rows[1]).all {
      prop(McRunDetailRow::lumpSumWithdrawal).isEqualTo(1_250_000)
      prop(McRunDetailRow::lumpSumTax).isEqualTo(250_000)
      prop(McRunDetailRow::lumpSumAmounts).containsExactly(1_000_000L)
      prop(McRunDetailRow::endBalance).isEqualTo(8_750_000)
    }
  }

  @Test
  fun `Draws the source pot first`() {
    val rows =
      run(
        lumpSum = McLumpSum(id = "l", age = 60, amount = 1_000_000.0, sourcePotId = "b"),
        pots = listOf(pot("a", 5_000_000.0), pot("b", 5_000_000.0)),
      )
    assertThat(rows[0].potLumpSums).containsExactly(0L, 1_000_000L)
  }

  @Test
  fun `A locked source pot falls back to the withdrawal order`() {
    val rows =
      run(
        lumpSum = McLumpSum(id = "l", age = 60, amount = 1_000_000.0, sourcePotId = "b"),
        pots = listOf(pot("a", 5_000_000.0), pot("b", 5_000_000.0).copy(accessAge = 70)),
      )
    assertThat(rows[0].potLumpSums).containsExactly(1_000_000L, 0L)
  }

  @Test
  fun `A lump sum the pots can't fund fails the plan`() {
    val config =
      config(
        lumpSum = McLumpSum(id = "l", age = 62, amount = 2_000_000.0),
        pots = listOf(pot("a", 1_000_000.0)),
      )
    val result = runMonteCarlo(config, deflate = false, captureRunDetail = 0)
    assertThat(result.successRate).isEqualTo(0.0)
    assertThat(result.runDetail.orEmpty()[2]).all {
      prop(McRunDetailRow::lumpSumWithdrawal).isEqualTo(1_000_000)
      prop(McRunDetailRow::lumpSumAmounts).containsExactly(2_000_000L)
      prop(McRunDetailRow::withdrawal).isZero()
    }
  }

  @Test
  fun `Drops lump sums without an age and round trips the rest`() {
    val meta =
      MonteCarloReportMeta(
        lumpSums =
          listOf(
            LumpSum(id = "a", name = "House", age = 65, amount = 5_000_000, sourcePotId = "p"),
            LumpSum(id = "b", amount = 100),
          ),
      )
    val config = meta.toConfig()
    assertThat(config.lumpSums)
      .containsExactly(
        McLumpSum(id = "a", age = 65, name = "House", amount = 5_000_000.0, sourcePotId = "p"),
      )
    assertThat(config.toMeta(meta).toConfig().lumpSums).isEqualTo(config.lumpSums)
  }

  @Test
  fun `Adds a lump sum a few years out, inside the plan`() {
    assertThat(McConfig(currentAge = 60, targetAge = 90).addLumpSum("l").lumpSums.single().age)
      .isEqualTo(65)
    assertThat(McConfig(currentAge = 60, targetAge = 63).addLumpSum("l").lumpSums.single().age)
      .isEqualTo(62)
  }

  @Test
  fun `Removing a pot sends its lump sums back to the withdrawal order`() {
    val config =
      McConfig(
        pots = persistentListOf(pot("a", 1.0), pot("b", 1.0)),
        lumpSums = persistentListOf(McLumpSum(id = "l", age = 65, sourcePotId = "b")),
      )
    assertThat(config.removePot("b").lumpSums.single().sourcePotId).isNull()
  }

  private fun pot(id: String, balance: Double) =
    McPot(
      id = id,
      startingBalance = balance,
      allocationPreset = Custom,
      expectedReturnMean = 0.0,
      returnStdDev = 0.0,
    )

  private fun config(lumpSum: McLumpSum, pots: List<McPot>) =
    McConfig(
      pots = pots.toImmutableList(),
      spendingPhases = persistentListOf(McSpendingPhase(annualWithdrawal = 0.0)),
      lumpSums = persistentListOf(lumpSum),
      inflationMean = null,
      simulationCount = 10,
    )

  private fun run(
    lumpSum: McLumpSum,
    pots: List<McPot> = listOf(pot("a", 10_000_000.0)),
  ): List<McRunDetailRow> =
    runMonteCarlo(config(lumpSum, pots), deflate = false, captureRunDetail = 0).runDetail.orEmpty()
}
