package aktual.budget.reports.vm

import assertk.all
import assertk.assertThat
import assertk.assertions.index
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import assertk.assertions.prop
import kotlin.test.Test
import kotlin.test.assertIs
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.json.Json
import org.intellij.lang.annotations.Language

class MonteCarloTest {
  // Expected values come from running upstream's runMonteCarloSimulation() on the same meta
  @Test
  fun `Default plan matches upstream`() =
    assertMatchesUpstream(
      meta = DEFAULT_PLAN,
      successRate = 0.7674,
      medianEndingBalance = 21250885,
      medianDepletionYear = 26,
      simulationCount = 5000,
      middle =
        McPercentileBand(
          year = 15,
          p5 = 12838283,
          p10 = 17320711,
          p25 = 26556531,
          p30 = 29113078,
          p50 = 38742445,
          p70 = 51389480,
          p75 = 55751099,
          p90 = 75641635,
        ),
      last =
        McPercentileBand(
          year = 30,
          p5 = 0,
          p10 = 0,
          p25 = 1458718,
          p30 = 5204423,
          p50 = 21250885,
          p70 = 44263951,
          p75 = 52591882,
          p90 = 94658990,
        ),
      medianTotalWithdrawn = 60000000,
      earliestDepletionYear = 14,
      latestDepletionYear = 30,
      worstRanked = listOf(2858, 1552, 3334, 135, 307),
      worstRunPathSum = 283822454L,
    )

  @Test
  fun `Historical bootstrap with a custom mix matches upstream`() =
    assertMatchesUpstream(
      meta = BOOTSTRAP_CUSTOM_MIX,
      successRate = 0.8385,
      medianEndingBalance = 289636115,
      medianDepletionYear = 29,
      simulationCount = 2000,
      middle =
        McPercentileBand(
          year = 20,
          p5 = 15035574,
          p10 = 34828946,
          p25 = 77733440,
          p30 = 91205002,
          p50 = 145082390,
          p70 = 231206453,
          p75 = 262895693,
          p90 = 422743677,
        ),
      last =
        McPercentileBand(
          year = 40,
          p5 = 0,
          p10 = 0,
          p25 = 59827455,
          p30 = 97349947,
          p50 = 289636115,
          p70 = 598375451,
          p75 = 727147679,
          p90 = 1553891447,
        ),
      medianTotalWithdrawn = 160000000,
      earliestDepletionYear = 15,
      latestDepletionYear = 40,
      worstRanked = listOf(654, 1336, 278, 1164, 1446),
      worstRunPathSum = 572016274L,
    )

  @Test
  fun `Historical sequence with sequential withdrawals matches upstream`() =
    assertMatchesUpstream(
      meta = SEQUENCE_SEQUENTIAL,
      successRate = 0.9183673469387755,
      medianEndingBalance = 132103675,
      medianDepletionYear = 24,
      simulationCount = 98,
      middle =
        McPercentileBand(
          year = 15,
          p5 = 33331331,
          p10 = 45999053,
          p25 = 77518450,
          p30 = 89481351,
          p50 = 120930560,
          p70 = 153823025,
          p75 = 162256371,
          p90 = 204729651,
        ),
      last =
        McPercentileBand(
          year = 30,
          p5 = 0,
          p10 = 6005808,
          p25 = 56254754,
          p30 = 65765124,
          p50 = 132103675,
          p70 = 180712221,
          p75 = 194819662,
          p90 = 290738464,
        ),
      medianTotalWithdrawn = 135000000,
      earliestDepletionYear = 22,
      latestDepletionYear = 28,
      worstRanked = listOf(38, 41, 37, 40, 45),
      worstRunPathSum = 1152175367L,
    )

  @Test
  fun `Guardrails with a locked pot and spending phases matches upstream`() =
    assertMatchesUpstream(
      meta = GUARDRAILS_LOCKED_POT,
      successRate = 0.8824,
      medianEndingBalance = 77669694,
      medianDepletionYear = 35,
      simulationCount = 5000,
      middle =
        McPercentileBand(
          year = 20,
          p5 = 28514979,
          p10 = 38522447,
          p25 = 58692059,
          p30 = 64008675,
          p50 = 85168007,
          p70 = 111596916,
          p75 = 119459384,
          p90 = 158023481,
        ),
      last =
        McPercentileBand(
          year = 40,
          p5 = 0,
          p10 = 0,
          p25 = 28291486,
          p30 = 37323651,
          p50 = 77669694,
          p70 = 127956182,
          p75 = 143975036,
          p90 = 221184804,
        ),
      medianTotalWithdrawn = 107920622,
      earliestDepletionYear = 16,
      latestDepletionYear = 40,
      worstRanked = listOf(3760, 3001, 1257, 3934, 4556),
      worstRunPathSum = 506013973L,
    )

  @Test
  fun `Ratcheting with target mix withdrawals matches upstream`() =
    assertMatchesUpstream(
      meta = RATCHETING_TARGET_MIX,
      successRate = 0.8856,
      medianEndingBalance = 61493857,
      medianDepletionYear = 27,
      simulationCount = 5000,
      middle =
        McPercentileBand(
          year = 15,
          p5 = 33820332,
          p10 = 43162047,
          p25 = 61710809,
          p30 = 67254317,
          p50 = 87856123,
          p70 = 114347875,
          p75 = 122130673,
          p90 = 161159372,
        ),
      last =
        McPercentileBand(
          year = 30,
          p5 = 0,
          p10 = 0,
          p25 = 22718308,
          p30 = 31215901,
          p50 = 61493857,
          p70 = 101975003,
          p75 = 114338597,
          p90 = 194394873,
        ),
      medianTotalWithdrawn = 107925147,
      earliestDepletionYear = 15,
      latestDepletionYear = 30,
      worstRanked = listOf(1467, 2807, 4904, 900, 3986),
      worstRunPathSum = 596959011L,
    )

  @Test
  fun `Floor and ceiling with fees matches upstream`() =
    assertMatchesUpstream(
      meta = FLOOR_CEILING_FEES,
      successRate = 0.5688,
      medianEndingBalance = 6351344,
      medianDepletionYear = 25,
      simulationCount = 5000,
      middle =
        McPercentileBand(
          year = 15,
          p5 = 17232273,
          p10 = 23261253,
          p25 = 36829588,
          p30 = 39970904,
          p50 = 54357231,
          p70 = 70088187,
          p75 = 74951548,
          p90 = 96940153,
        ),
      last =
        McPercentileBand(
          year = 30,
          p5 = 0,
          p10 = 0,
          p25 = 0,
          p30 = 0,
          p50 = 6351344,
          p70 = 30223261,
          p75 = 37179581,
          p90 = 72710922,
        ),
      medianTotalWithdrawn = 108963396,
      earliestDepletionYear = 14,
      latestDepletionYear = 30,
      worstRanked = listOf(838, 4784, 903, 1739, 2515),
      worstRunPathSum = 497462713L,
    )

  @Test
  fun `Boundaries with tax bands and income matches upstream`() =
    assertMatchesUpstream(
      meta = BOUNDARIES_TAX_BANDS,
      successRate = 0.8402,
      medianEndingBalance = 30824907,
      medianDepletionYear = 35,
      simulationCount = 5000,
      middle =
        McPercentileBand(
          year = 20,
          p5 = 21382222,
          p10 = 28788222,
          p25 = 41440704,
          p30 = 44829150,
          p50 = 59615405,
          p70 = 79823401,
          p75 = 85693976,
          p90 = 118387223,
        ),
      last =
        McPercentileBand(
          year = 40,
          p5 = 0,
          p10 = 0,
          p25 = 9832238,
          p30 = 14217514,
          p50 = 30824907,
          p70 = 48065357,
          p75 = 54494300,
          p90 = 91903528,
        ),
      medianTotalWithdrawn = 128155311,
      earliestDepletionYear = 17,
      latestDepletionYear = 40,
      worstRanked = listOf(1161, 1737, 2966, 4755, 906),
      worstRunPathSum = 473531399L,
    )

  @Test
  fun `Income streams, contributions and a surplus pot matches upstream`() =
    assertMatchesUpstream(
      meta = INCOME_CONTRIBUTIONS_SURPLUS,
      successRate = 0.6093333333333333,
      medianEndingBalance = 29955939,
      medianDepletionYear = 42,
      simulationCount = 3000,
      middle =
        McPercentileBand(
          year = 28,
          p5 = 6307650,
          p10 = 16533238,
          p25 = 36624895,
          p30 = 42130125,
          p50 = 66909473,
          p70 = 99417306,
          p75 = 111222548,
          p90 = 167485495,
        ),
      last =
        McPercentileBand(
          year = 55,
          p5 = 0,
          p10 = 0,
          p25 = 0,
          p30 = 0,
          p50 = 29955939,
          p70 = 111892094,
          p75 = 143538177,
          p90 = 306205678,
        ),
      medianTotalWithdrawn = 118888474,
      earliestDepletionYear = 22,
      latestDepletionYear = 55,
      worstRanked = listOf(79, 357, 741, 1687, 720),
      worstRunPathSum = 663418556L,
    )

  @Test
  fun `Inflation disabled matches upstream`() =
    assertMatchesUpstream(
      meta = INFLATION_DISABLED,
      successRate = 0.959,
      medianEndingBalance = 216765513,
      medianDepletionYear = 25,
      simulationCount = 1000,
      middle =
        McPercentileBand(
          year = 15,
          p5 = 26306935,
          p10 = 38182227,
          p25 = 59319611,
          p30 = 66061440,
          p50 = 94186854,
          p70 = 123102130,
          p75 = 136366456,
          p90 = 183821418,
        ),
      last =
        McPercentileBand(
          year = 30,
          p5 = 7265809,
          p10 = 35918967,
          p25 = 105303930,
          p30 = 126755283,
          p50 = 216765513,
          p70 = 347768621,
          p75 = 402682413,
          p90 = 642318564,
        ),
      medianTotalWithdrawn = 75000000,
      earliestDepletionYear = 17,
      latestDepletionYear = 30,
      worstRanked = listOf(896, 359, 939, 46, 523),
      worstRunPathSum = 316874827L,
    )

  @Test
  fun `Always succeeds with no withdrawals`() {
    val result =
      runMonteCarlo(
        McConfig(spendingPhases = persistentListOf(McSpendingPhase(annualWithdrawal = 0.0)))
      )
    assertThat(result).all {
      prop(McResult::successRate).isEqualTo(1.0)
      prop(McResult::medianDepletionYear).isNull()
    }
  }

  @Test
  fun `Depletes in year one when the withdrawal exceeds the pot`() {
    val config =
      McConfig(
        pots = persistentListOf(McPot(id = "a", startingBalance = 1_000_000.0)),
        spendingPhases = persistentListOf(McSpendingPhase(annualWithdrawal = 2_000_000.0)),
      )
    val result = runMonteCarlo(config)
    assertThat(result).all {
      prop(McResult::successRate).isEqualTo(0.0)
      prop(McResult::medianDepletionYear).isEqualTo(1)
      prop(McResult::percentileBands).index(1).prop(McPercentileBand::p90).isEqualTo(0L)
    }
  }

  @Test
  fun `Matches the closed-form depletion year with zero volatility`() {
    // 100k growing 5% a year with 10k withdrawn at the start of each year runs dry in year 14
    val config =
      McConfig(
        pots =
          persistentListOf(
            McPot(
              id = "a",
              startingBalance = 10_000_000.0,
              allocationPreset = Custom,
              expectedReturnMean = 0.05,
              returnStdDev = 0.0,
            )
          ),
        spendingPhases = persistentListOf(McSpendingPhase(annualWithdrawal = 1_000_000.0)),
        inflationMean = null,
        currentAge = 60,
        targetAge = 90,
      )
    val result = runMonteCarlo(config)
    assertThat(result).all {
      prop(McResult::successRate).isEqualTo(0.0)
      prop(McResult::medianDepletionYear).isEqualTo(14)
    }
  }

  @Test
  fun `Custom mix only counts when it totals 100 percent`() {
    val pot = McPot(id = "a", allocationPreset = CustomMix)
    assertThat(
        pot.copy(allocationStocks = 0.7, allocationBonds = 0.3, allocationCash = 0.0).assetWeights()
      )
      .isEqualTo(AssetWeights(stocks = 0.7, bonds = 0.3, cash = 0.0))
    assertThat(
        pot.copy(allocationStocks = 0.5, allocationBonds = 0.3, allocationCash = 0.0).assetWeights()
      )
      .isNull()
    assertThat(pot.copy(allocationPreset = Custom).assetWeights()).isNull()
  }

  @Test
  fun `Config survives a round trip through the meta`() {
    for (plan in
      listOf(GUARDRAILS_LOCKED_POT, BOUNDARIES_TAX_BANDS, INCOME_CONTRIBUTIONS_SURPLUS)) {
      val meta = decode(plan)
      val config = meta.toConfig()
      assertThat(config.toMeta(meta).toConfig()).isEqualTo(config)
    }
  }

  private fun decode(meta: String): MonteCarloReportMeta =
    assertIs<MonteCarloReportMeta>(Json.decodeFromString(ReportMeta.serializer(MonteCarlo), meta))

  @Suppress("LongParameterList")
  private fun assertMatchesUpstream(
    @Language("JSON") meta: String,
    successRate: Double,
    medianEndingBalance: Long,
    medianDepletionYear: Int?,
    simulationCount: Int,
    middle: McPercentileBand,
    last: McPercentileBand,
    medianTotalWithdrawn: Long,
    earliestDepletionYear: Int?,
    latestDepletionYear: Int?,
    worstRanked: List<Int>,
    worstRunPathSum: Long,
  ) {
    val result = runMonteCarlo(decode(meta).toConfig())
    assertThat(result).all {
      prop(McResult::successRate).isEqualTo(successRate)
      prop(McResult::medianEndingBalance).isEqualTo(medianEndingBalance)
      prop(McResult::medianDepletionYear).isEqualTo(medianDepletionYear)
      prop(McResult::simulationCount).isEqualTo(simulationCount)
      prop(McResult::percentileBands).index(middle.year).isEqualTo(middle)
      prop(McResult::percentileBands).transform { it.last() }.isEqualTo(last)
      prop(McResult::medianTotalWithdrawn).isEqualTo(medianTotalWithdrawn)
      prop(McResult::earliestDepletionYear).isEqualTo(earliestDepletionYear)
      prop(McResult::latestDepletionYear).isEqualTo(latestDepletionYear)
      prop(McResult::worstRunPath).transform { it.sum() }.isEqualTo(worstRunPathSum)
      transform { rankSimulationsWorstFirst(it.endingBalances, it.depletionYearBySimulation) }
        .transform { it.take(worstRanked.size) }
        .isEqualTo(worstRanked)
    }
  }
}
