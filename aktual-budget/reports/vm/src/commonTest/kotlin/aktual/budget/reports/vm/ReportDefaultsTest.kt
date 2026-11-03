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
          conditions = listOf(),
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
          conditions = listOf(),
          conditionsOp = And,
          timeFrame = TimeFrame(CURRENT, CURRENT, Full),
        ),
      )
  }

  @Test
  fun `Age of money defaults to the last six months, monthly`() {
    assertThat(
        AgeOfMoneyReportMeta(granularity = AgeOfMoneyGranularity.Unknown).withDefaults(CURRENT),
      )
      .isEqualTo(
        AgeOfMoneyReportMeta(
          conditions = listOf(),
          conditionsOp = And,
          timeFrame = TimeFrame(YearMonth(2025, 8), CURRENT, SlidingWindow),
          granularity = AgeOfMoneyGranularity.Monthly,
        ),
      )
  }

  @Test
  fun `Saved settings are kept`() {
    val meta =
      CashFlowReportMeta(
        conditions = listOf(),
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
