package aktual.budget.reports.vm

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth

class SpendingTest {
  @Test
  fun `Average range covers the months before the compared month`() {
    assertThat(SpendingAverageRange.LastMonths(3).resolve(JUNE, earliest = null))
      .containsExactly(YearMonth(2026, 3), YearMonth(2026, 4), YearMonth(2026, 5))
    assertThat(null.resolve(JUNE, earliest = null)).isEqualTo(SpendingAverageRange.Default.resolve(JUNE, null))
  }

  @Test
  fun `Year to date average starts in January`() {
    assertThat(SpendingAverageRange.YearToDate.resolve(YearMonth(2026, 3), earliest = null))
      .containsExactly(YearMonth(2026, 1), YearMonth(2026, 2))
    assertThat(SpendingAverageRange.YearToDate.resolve(YearMonth(2026, 1), earliest = null)).isEmpty()
  }

  @Test
  fun `All time average starts at the earliest transaction`() {
    assertThat(SpendingAverageRange.AllTime.resolve(JUNE, earliest = YearMonth(2026, 4)))
      .containsExactly(YearMonth(2026, 4), YearMonth(2026, 5))
    assertThat(SpendingAverageRange.AllTime.resolve(JUNE, earliest = null)).isEmpty()
    assertThat(SpendingAverageRange.AllTime.resolve(JUNE, earliest = JUNE)).isEmpty()
  }

  @Test
  fun `Missing months compare this month with last month`() {
    assertThat(SpendingReportMeta().resolveMonths(TODAY))
      .isEqualTo(SpendingMonths(JUNE, YearMonth(2026, 5)))
  }

  @Test
  fun `Missing compare to defaults to the month before compare`() {
    val meta = SpendingReportMeta(compare = "2026-02", isLive = false)
    assertThat(meta.resolveMonths(TODAY))
      .isEqualTo(SpendingMonths(YearMonth(2026, 2), YearMonth(2026, 1)))
  }

  @Test
  fun `Live single month keeps the gap between the months`() {
    val meta = SpendingReportMeta(compare = "2025-12", compareTo = "2025-09")
    assertThat(meta.resolveMonths(TODAY)).isEqualTo(SpendingMonths(JUNE, YearMonth(2026, 3)))
  }

  @Test
  fun `Static budget and average modes keep the saved month`() {
    for (mode in listOf(SpendingMode.Budget, SpendingMode.Average)) {
      val meta = SpendingReportMeta(mode = mode, compare = "2026-02", isLive = false)
      assertThat(meta.resolveMonths(TODAY).compare).isEqualTo(YearMonth(2026, 2))
    }
  }

  @Test
  fun `Live budget and average modes chart this month`() {
    for (mode in listOf(SpendingMode.Budget, SpendingMode.Average)) {
      val meta = SpendingReportMeta(mode = mode, compare = "2026-02", compareTo = "2025-01")
      assertThat(meta.resolveMonths(TODAY)).isEqualTo(SpendingMonths(JUNE, JUNE))
    }
  }

  private companion object {
    val TODAY = LocalDate(2026, 6, 15)
    val JUNE = YearMonth(2026, 6)
  }
}
