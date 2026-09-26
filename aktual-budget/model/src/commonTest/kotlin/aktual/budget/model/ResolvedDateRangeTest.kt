package aktual.budget.model

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlinx.datetime.LocalDate

class ResolvedDateRangeTest {
  @Test
  fun `Unknown range resolves the same as all time`() {
    val today = LocalDate(2026, 9, 26)

    assertThat(DateRangeType.Unknown.resolve(today)).isEqualTo(DateRangeType.AllTime.resolve(today))
  }

  @Test
  fun `Current quarter covers the whole of this quarter`() {
    val today = LocalDate(2026, 8, 15)

    assertThat(DateRangeType.CurrentQuarter.resolve(today))
      .isEqualTo(ResolvedDateRange(LocalDate(2026, 7, 1), LocalDate(2026, 9, 30)))
  }

  @Test
  fun `Previous quarter covers the whole of the last quarter`() {
    val today = LocalDate(2026, 2, 10)

    assertThat(DateRangeType.PreviousQuarter.resolve(today))
      .isEqualTo(ResolvedDateRange(LocalDate(2025, 10, 1), LocalDate(2025, 12, 31)))
  }

  @Test
  fun `Last 30 days includes today`() {
    val today = LocalDate(2026, 3, 5)

    assertThat(DateRangeType.Last30Days.resolve(today))
      .isEqualTo(ResolvedDateRange(LocalDate(2026, 2, 4), today))
  }

  @Test
  fun `Prior year to date ends on the same day last year`() {
    val today = LocalDate(2026, 9, 26)

    assertThat(DateRangeType.PriorYearToDate.resolve(today))
      .isEqualTo(ResolvedDateRange(LocalDate(2025, 1, 1), LocalDate(2025, 9, 26)))
  }
}
