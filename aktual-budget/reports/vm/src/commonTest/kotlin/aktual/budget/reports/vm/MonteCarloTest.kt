package aktual.budget.reports.vm

import assertk.all
import assertk.assertThat
import assertk.assertions.index
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import assertk.assertions.prop
import kotlin.test.Test
import kotlin.test.assertIs
import kotlinx.serialization.json.Json
import org.intellij.lang.annotations.Language

class MonteCarloTest {
  // Expected values come from running upstream's runMonteCarloSimulation() on the same meta
  @Test
  fun `Default plan matches upstream`() =
    assertMatchesUpstream(
      meta = "{}",
      successRate = 0.7674,
      medianEndingBalance = 21250885,
      medianDepletionYear = 26,
      simulationCount = 5000,
      middle = McPercentileBand(15, 17320711, 26556531, 38742445, 55751099, 75641635),
      last = McPercentileBand(30, 0, 1458718, 21250885, 52591882, 94658990),
    )

  @Test
  fun `Historical bootstrap with a custom mix matches upstream`() =
    assertMatchesUpstream(
      meta =
        """
        {
          "returnModel": "historical-bootstrap",
          "simulationCount": 2000,
          "pots": [
            {
              "id": "a",
              "startingBalance": 80000000,
              "allocationPreset": "equity-80"
            },
            {
              "id": "b",
              "startingBalance": 20000000,
              "allocationPreset": "custom-mix",
              "allocationStocks": 0.3,
              "allocationBonds": 0.5,
              "allocationCash": 0.2
            }
          ],
          "spendingPhases": [
            {
              "id": "p1",
              "annualWithdrawal": 4000000
            }
          ],
          "currentAge": 55,
          "targetAge": 95
        }
        """
          .trimIndent(),
      successRate = 0.8385,
      medianEndingBalance = 289636115,
      medianDepletionYear = 29,
      simulationCount = 2000,
      middle = McPercentileBand(20, 34828946, 77733440, 145082390, 262895693, 422743677),
      last = McPercentileBand(40, 0, 59827455, 289636115, 727147679, 1553891447),
    )

  @Test
  fun `Historical sequence with sequential withdrawals matches upstream`() =
    assertMatchesUpstream(
      meta =
        """
        {
          "returnModel": "historical-sequence",
          "pots": [
            {
              "id": "a",
              "startingBalance": 100000000,
              "allocationPreset": "equity-60"
            },
            {
              "id": "c",
              "startingBalance": 5000000,
              "allocationPreset": "custom",
              "expectedReturnMean": 0.04,
              "returnStdDev": 0.05
            }
          ],
          "spendingPhases": [
            {
              "id": "p1",
              "annualWithdrawal": 4500000
            }
          ],
          "withdrawalStrategy": "sequential"
        }
        """
          .trimIndent(),
      successRate = 0.9183673469387755,
      medianEndingBalance = 132103675,
      medianDepletionYear = 24,
      simulationCount = 98,
      middle = McPercentileBand(15, 45999053, 77518450, 120930560, 162256371, 204729651),
      last = McPercentileBand(30, 6005808, 56254754, 132103675, 194819662, 290738464),
    )

  @Test
  fun `Guardrails with a locked pot and spending phases matches upstream`() =
    assertMatchesUpstream(
      meta =
        """
        {
          "withdrawalRule": {
            "type": "guardrails"
          },
          "minimumSpending": 2500000,
          "spendingPhases": [
            {
              "id": "p1",
              "annualWithdrawal": 3000000
            },
            {
              "id": "p2",
              "fromAge": 75,
              "annualWithdrawal": 2200000
            }
          ],
          "pots": [
            {
              "id": "a",
              "startingBalance": 70000000,
              "allocationPreset": "equity-80",
              "accessAge": 57
            },
            {
              "id": "b",
              "startingBalance": 20000000,
              "allocationPreset": "cash"
            }
          ],
          "currentAge": 52,
          "targetAge": 92,
          "withdrawalStrategy": "best-performer"
        }
        """
          .trimIndent(),
      successRate = 0.8824,
      medianEndingBalance = 77669694,
      medianDepletionYear = 35,
      simulationCount = 5000,
      middle = McPercentileBand(20, 38522447, 58692059, 85168007, 119459384, 158023481),
      last = McPercentileBand(40, 0, 28291486, 77669694, 143975036, 221184804),
    )

  @Test
  fun `Ratcheting with target mix withdrawals matches upstream`() =
    assertMatchesUpstream(
      meta =
        """
        {
          "withdrawalRule": {
            "type": "ratcheting",
            "consecutiveYears": 2
          },
          "pots": [
            {
              "id": "a",
              "startingBalance": 60000000,
              "allocationPreset": "equity-100"
            },
            {
              "id": "b",
              "startingBalance": 40000000,
              "allocationPreset": "equity-40"
            }
          ],
          "withdrawalStrategy": "target-mix",
          "spendingPhases": [
            {
              "id": "p1",
              "annualWithdrawal": 3500000
            }
          ]
        }
        """
          .trimIndent(),
      successRate = 0.8856,
      medianEndingBalance = 61493857,
      medianDepletionYear = 27,
      simulationCount = 5000,
      middle = McPercentileBand(15, 43162047, 61710809, 87856123, 122130673, 161159372),
      last = McPercentileBand(30, 0, 22718308, 61493857, 114338597, 194394873),
    )

  @Test
  fun `Floor and ceiling with fees matches upstream`() =
    assertMatchesUpstream(
      meta =
        """
        {
          "withdrawalRule": {
            "type": "floor-ceiling",
            "floorPct": 0.1,
            "ceilingPct": 0.25
          },
          "inflationStdDev": 0,
          "inflationMean": 0.03,
          "pots": [
            {
              "id": "a",
              "startingBalance": 90000000,
              "annualFeeRate": 0.005,
              "annualFeeFixed": 50000,
              "feeAdjustsWithInflation": true
            }
          ],
          "spendingPhases": [
            {
              "id": "p1",
              "annualWithdrawal": 4000000
            }
          ]
        }
        """
          .trimIndent(),
      successRate = 0.5688,
      medianEndingBalance = 6351344,
      medianDepletionYear = 25,
      simulationCount = 5000,
      middle = McPercentileBand(15, 23261253, 36829588, 54357231, 74951548, 96940153),
      last = McPercentileBand(30, 0, 0, 6351344, 37179581, 72710922),
    )

  @Test
  fun `Boundaries with tax bands and income matches upstream`() =
    assertMatchesUpstream(
      meta =
        """
        {
          "withdrawalRule": {
            "type": "boundaries"
          },
          "minimumSpending": 3000000,
          "taxModel": "bands",
          "taxBands": [
            {
              "id": "b1",
              "from": 1257000,
              "rate": 0.2
            },
            {
              "id": "b2",
              "from": 5027000,
              "rate": 0.4
            }
          ],
          "pots": [
            {
              "id": "pen",
              "startingBalance": 60000000,
              "taxableFraction": 0.75,
              "accessAge": 57
            },
            {
              "id": "isa",
              "startingBalance": 30000000,
              "taxableFraction": 0
            }
          ],
          "incomeStreams": [
            {
              "id": "sp",
              "fromAge": 67,
              "annualAmount": 1150000,
              "taxableFraction": 1
            }
          ],
          "currentAge": 55,
          "targetAge": 95,
          "spendingPhases": [
            {
              "id": "p1",
              "annualWithdrawal": 4000000
            }
          ]
        }
        """
          .trimIndent(),
      successRate = 0.8402,
      medianEndingBalance = 30824907,
      medianDepletionYear = 35,
      simulationCount = 5000,
      middle = McPercentileBand(20, 28788222, 41440704, 59615405, 85693976, 118387223),
      last = McPercentileBand(40, 0, 9832238, 30824907, 54494300, 91903528),
    )

  @Test
  fun `Income streams, contributions and a surplus pot matches upstream`() =
    assertMatchesUpstream(
      meta =
        """
        {
          "pots": [
            {
              "id": "surplus",
              "isSurplus": true
            },
            {
              "id": "a",
              "startingBalance": 40000000,
              "withdrawalTaxRate": 0.15
            }
          ],
          "incomeStreams": [
            {
              "id": "job",
              "toAge": 50,
              "annualAmount": 6000000,
              "taxRate": 0.3
            },
            {
              "id": "db",
              "fromAge": 60,
              "annualAmount": 1500000,
              "adjustsWithInflation": false,
              "taxRate": 0.1
            }
          ],
          "contributions": [
            {
              "id": "c1",
              "potId": "a",
              "toAge": 50,
              "annualAmount": 1000000,
              "sourceIncomeStreamId": "job",
              "beforeTax": true
            },
            {
              "id": "c2",
              "potId": "a",
              "toAge": 50,
              "annualAmount": 500000,
              "sourceIncomeStreamId": "job"
            },
            {
              "id": "c3",
              "potId": "a",
              "fromAge": 45,
              "toAge": 55,
              "annualAmount": 300000,
              "adjustsWithInflation": false
            }
          ],
          "spendingPhases": [
            {
              "id": "p1",
              "annualWithdrawal": 2800000
            }
          ],
          "currentAge": 40,
          "targetAge": 95,
          "simulationCount": 3000
        }
        """
          .trimIndent(),
      successRate = 0.6093333333333333,
      medianEndingBalance = 29955939,
      medianDepletionYear = 42,
      simulationCount = 3000,
      middle = McPercentileBand(28, 16533238, 36624895, 66909473, 111222548, 167485495),
      last = McPercentileBand(55, 0, 0, 29955939, 143538177, 306205678),
    )

  @Test
  fun `Inflation disabled matches upstream`() =
    assertMatchesUpstream(
      meta =
        """
        {
          "inflationMean": null,
          "returnModel": "historical-bootstrap",
          "simulationCount": 1000,
          "pots": [
            {
              "id": "a",
              "startingBalance": 50000000,
              "allocationPreset": "equity-60"
            }
          ],
          "spendingPhases": [
            {
              "id": "p1",
              "annualWithdrawal": 2500000
            }
          ]
        }
        """
          .trimIndent(),
      successRate = 0.959,
      medianEndingBalance = 216765513,
      medianDepletionYear = 25,
      simulationCount = 1000,
      middle = McPercentileBand(15, 38182227, 59319611, 94186854, 136366456, 183821418),
      last = McPercentileBand(30, 35918967, 105303930, 216765513, 402682413, 642318564),
    )

  @Test
  fun `Always succeeds with no withdrawals`() {
    val result =
      runMonteCarlo(McConfig(spendingPhases = listOf(McSpendingPhase(annualWithdrawal = 0.0))))
    assertThat(result).all {
      prop(McResult::successRate).isEqualTo(1.0)
      prop(McResult::medianDepletionYear).isNull()
    }
  }

  @Test
  fun `Depletes in year one when the withdrawal exceeds the pot`() {
    val config =
      McConfig(
        pots = listOf(McPot(id = "a", startingBalance = 1_000_000.0)),
        spendingPhases = listOf(McSpendingPhase(annualWithdrawal = 2_000_000.0)),
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
          listOf(
            McPot(
              id = "a",
              startingBalance = 10_000_000.0,
              allocationPreset = Custom,
              expectedReturnMean = 0.05,
              returnStdDev = 0.0,
            )
          ),
        spendingPhases = listOf(McSpendingPhase(annualWithdrawal = 1_000_000.0)),
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

  private fun assertMatchesUpstream(
    @Language("JSON") meta: String,
    successRate: Double,
    medianEndingBalance: Long,
    medianDepletionYear: Int?,
    simulationCount: Int,
    middle: McPercentileBand,
    last: McPercentileBand,
  ) {
    val decoded = Json.decodeFromString(ReportMeta.serializer(MonteCarlo), meta)
    val result = runMonteCarlo(assertIs<MonteCarloReportMeta>(decoded).toConfig())
    assertThat(result).all {
      prop(McResult::successRate).isEqualTo(successRate)
      prop(McResult::medianEndingBalance).isEqualTo(medianEndingBalance)
      prop(McResult::medianDepletionYear).isEqualTo(medianDepletionYear)
      prop(McResult::simulationCount).isEqualTo(simulationCount)
      prop(McResult::percentileBands).index(middle.year).isEqualTo(middle)
      prop(McResult::percentileBands).transform { it.last() }.isEqualTo(last)
    }
  }
}
