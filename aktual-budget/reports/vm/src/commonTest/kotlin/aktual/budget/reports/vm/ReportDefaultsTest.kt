package aktual.budget.reports.vm

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlinx.datetime.YearMonth

class ReportDefaultsTest {
  @Test
  fun `Cash flow defaults to this month with the balance shown`() {
    assertThat(CashFlowReportMeta(name = "Cash").withDefaults(CURRENT))
      .isEqualTo(
        CashFlowReportMeta(
          name = "Cash",
          conditions = [],
          conditionsOp = And,
          timeFrame = TimeFrame(CURRENT, CURRENT, SlidingWindow),
          showBalance = true,
        ),
      )
  }

  @Test
  fun `Calendar defaults to the full range`() {
    assertThat(CalendarReportMeta().withDefaults(CURRENT))
      .isEqualTo(
        CalendarReportMeta(
          conditions = [],
          conditionsOp = And,
          timeFrame = TimeFrame(CURRENT, CURRENT, Full),
        ),
      )
  }

  @Test
  fun `Age of money defaults to the last six months, monthly`() {
    assertThat(
        AgeOfMoneyReportMeta(granularity = Unknown).withDefaults(CURRENT),
      )
      .isEqualTo(
        AgeOfMoneyReportMeta(
          conditions = [],
          conditionsOp = And,
          timeFrame = TimeFrame(YearMonth(2025, 8), CURRENT, SlidingWindow),
          granularity = Monthly,
        ),
      )
  }

  @Test
  fun `Crossover defaults to ten years up to last month`() {
    assertThat(CrossoverReportMeta(projectionType = Unknown).withDefaults(CURRENT))
      .isEqualTo(
        CrossoverReportMeta(
          timeFrame = TimeFrame(YearMonth(2016, 1), YearMonth(2025, 12), Full),
          safeWithdrawalRate = 0.04,
          projectionType = Hampel,
          showHiddenCategories = false,
          expenseAdjustmentFactor = 1.0,
        ),
      )
  }

  @Test
  fun `Balance forecast defaults to the next twelve months of schedules`() {
    assertThat(BalanceForecastReportMeta().withDefaults(CURRENT))
      .isEqualTo(
        BalanceForecastReportMeta(
          conditions = [],
          conditionsOp = And,
          timeFrame = TimeFrame(YearMonth(2026, 1), YearMonth(2026, 12), Static),
          granularity = Monthly,
          source = Schedules,
        ),
      )
  }

  @Test
  fun `Saved settings are kept`() {
    val meta =
      CashFlowReportMeta(
        conditions = [],
        conditionsOp = Or,
        timeFrame = TimeFrame(YearMonth(2024, 1), YearMonth(2024, 3), Static),
        showBalance = false,
      )
    assertThat(meta.withDefaults(CURRENT)).isEqualTo(meta)
  }

  private companion object {
    val CURRENT = YearMonth(2026, 1)
  }
}
