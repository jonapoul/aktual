package aktual.budget.reports.vm

import aktual.budget.model.AccountId
import aktual.budget.model.CategoryId
import aktual.budget.model.WidgetType
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import kotlin.test.Test
import kotlinx.datetime.Month.JULY
import kotlinx.datetime.Month.OCTOBER
import kotlinx.datetime.YearMonth
import kotlinx.serialization.json.Json

class ReportMetaSerializationTest {
  private val json = Json

  @Test
  fun `TimeFrame parses a full ISO date by truncating to year-month`() {
    // Regression: upstream stores start/end as full dates like "2011-10-05", which used to crash
    // the dashboard because the field is typed as a YearMonth.
    val decoded =
      json.decodeFromString(
        TimeFrame.serializer(),
        """{"start":"2011-10-05","end":"2025-07-31","mode":"static"}""",
      )

    assertThat(decoded.start).isEqualTo(YearMonth(2011, OCTOBER))
    assertThat(decoded.end).isEqualTo(YearMonth(2025, JULY))
    assertThat(decoded.mode).isEqualTo(Static)
  }

  @Test
  fun `TimeFrame parses a plain year-month`() {
    val decoded =
      json.decodeFromString(
        TimeFrame.serializer(),
        """{"start":"2011-10","end":"2025-07","mode":"sliding-window"}""",
      )

    assertThat(decoded.start).isEqualTo(YearMonth(2011, OCTOBER))
    assertThat(decoded.mode).isEqualTo(SlidingWindow)
  }

  @Test
  fun `FormulaReportMeta with a full-date query time frame decodes`() {
    // Mirrors the exact shape from the original crash: a formula query whose nested timeFrame
    // holds full dates.
    val element =
      json.parseToJsonElement(
        """
        {
          "queries": {
            "a": {
              "conditions": [],
              "conditionsOp": "and",
              "timeFrame": {"start":"2011-10-05","end":"2025-07-31","mode":"static"}
            }
          }
        }
        """
          .trimIndent()
      )

    val decoded = json.decodeFromJsonElement(ReportMeta.serializer(Formula), element)

    val formula = decoded as FormulaReportMeta
    assertThat(formula.queries.getValue("a").timeFrame?.start).isEqualTo(YearMonth(2011, OCTOBER))
    assertThat(formula.queries.getValue("a").conditionsOp).isEqualTo(And)
  }

  @Test
  fun `Unrecognised enum values decode as Unknown`() {
    val timeFrame =
      json.decodeFromString(
        TimeFrame.serializer(),
        """{"start":"2011-10","end":"2025-07","mode":"new-mode"}""",
      )
    assertThat(timeFrame.mode).isEqualTo(Unknown)

    val netWorth =
      json.decodeFromJsonElement(
        ReportMeta.serializer(NetWorth),
        json.parseToJsonElement("""{"mode":"new-mode"}"""),
      )
    assertThat((netWorth as NetWorthReportMeta).mode).isEqualTo(Unknown)
  }

  @Test
  fun `Crossover meta decodes`() {
    val meta =
      decode<CrossoverReportMeta>(
        Crossover,
        """
        {
          "name": "FI",
          "expenseCategoryIds": ["cat-1"],
          "incomeAccountIds": ["acct-1"],
          "timeFrame": {"start":"2024-01","end":"2025-01","mode":"static"},
          "safeWithdrawalRate": 0.04,
          "estimatedReturn": null,
          "projectionType": "hampel",
          "expenseAdjustmentFactor": 1.0
        }
        """
          .trimIndent(),
      )

    assertThat(meta.expenseCategoryIds).isEqualTo(listOf(CategoryId("cat-1")))
    assertThat(meta.incomeAccountIds).isEqualTo(listOf(AccountId("acct-1")))
    assertThat(meta.safeWithdrawalRate).isEqualTo(0.04)
    assertThat(meta.estimatedReturn).isNull()
    assertThat(meta.projectionType).isEqualTo(Hampel)
  }

  @Test
  fun `Sankey meta decodes`() {
    val meta =
      decode<SankeyReportMeta>(
        Sankey,
        """{"mode":"spent","topNcategories":5,"categorySort":"budget-order","showTransfers":true}""",
      )

    assertThat(meta.mode).isEqualTo(Spent)
    assertThat(meta.topNCategories).isEqualTo(5)
    assertThat(meta.categorySort).isEqualTo(BudgetOrder)
    assertThat(meta.showTransfers).isEqualTo(true)
  }

  @Test
  fun `Balance forecast meta decodes`() {
    val meta =
      decode<BalanceForecastReportMeta>(
        BalanceForecast,
        """{"accounts":["acct-1"],"granularity":"Monthly","source":"tracking-budget"}""",
      )

    assertThat(meta.accounts).isEqualTo(listOf(AccountId("acct-1")))
    assertThat(meta.granularity).isEqualTo(Monthly)
    assertThat(meta.source).isEqualTo(TrackingBudget)
  }

  @Test
  fun `Age of money meta decodes`() {
    val meta = decode<AgeOfMoneyReportMeta>(AgeOfMoney, """{"name":"AoM","granularity":"weekly"}""")

    assertThat(meta.name).isEqualTo("AoM")
    assertThat(meta.granularity).isEqualTo(Weekly)
  }

  @Test
  fun `Monte Carlo meta decodes`() {
    val meta =
      decode<MonteCarloReportMeta>(
        MonteCarlo,
        """
        {
          "pots": [
            {"id":"p1","startingBalance":1000000,"allocationPreset":"equity-80","accountId":null}
          ],
          "withdrawalStrategy": "target-mix",
          "returnModel": "historical-bootstrap",
          "withdrawalRule": {"type":"guardrails","prosperityTriggerPct":0.2},
          "spendingPhases": [{"id":"s1","fromAge":null,"annualWithdrawal":40000}],
          "contributions": [{"id":"c1","potId":"p1","annualAmount":500}],
          "incomeStreams": [{"id":"i1","fromAge":67,"taxRate":0.2}],
          "inflationMean": null,
          "taxModel": "bands",
          "taxBands": [{"id":"t1","from":1257000,"rate":0.2}],
          "currentAge": 40,
          "targetAge": 95,
          "simulationCount": 1000
        }
        """
          .trimIndent(),
      )

    val pot = meta.pots.orEmpty().single()
    assertThat(pot.startingBalance).isEqualTo(1000000L)
    assertThat(pot.allocationPreset).isEqualTo(Equity80)
    assertThat(pot.accountId).isNull()
    assertThat(meta.withdrawalStrategy).isEqualTo(TargetMix)
    assertThat(meta.returnModel).isEqualTo(HistoricalBootstrap)
    assertThat(meta.withdrawalRule?.type).isEqualTo(Guardrails)
    assertThat(meta.taxModel).isEqualTo(Bands)
    assertThat(meta.taxBands.orEmpty().single().from).isEqualTo(1257000L)
    assertThat(meta.targetAge).isEqualTo(95)
    assertThat(meta.inflationMean).isNull()
  }

  @Test
  fun `Monte Carlo inflation defaults when missing`() {
    val meta = decode<MonteCarloReportMeta>(MonteCarlo, "{}")
    assertThat(meta.inflationMean).isEqualTo(0.025)
  }

  @Test
  fun `Monte Carlo legacy minimumWithdrawal feeds minimum spending`() {
    val meta = decode<MonteCarloReportMeta>(MonteCarlo, """{"minimumWithdrawal": 100000}""")
    assertThat(meta.toConfig().minimumSpending).isEqualTo(100_000.0)
  }

  private inline fun <reified T : ReportMeta> decode(type: WidgetType, string: String): T =
    json.decodeFromJsonElement(ReportMeta.serializer(type), json.parseToJsonElement(string)) as T
}
