package aktual.budget.reports.vm

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month.APRIL
import kotlinx.datetime.Month.DECEMBER
import kotlinx.datetime.Month.FEBRUARY
import kotlinx.datetime.Month.JANUARY
import kotlinx.datetime.Month.JUNE
import kotlinx.datetime.Month.MARCH
import kotlinx.datetime.Month.MAY
import kotlinx.datetime.YearMonth

class CrossoverRangeTest {
  private val today = LocalDate(2026, MAY, 15)

  private fun range(
    mode: TimeFrameMode?,
    earliest: LocalDate? = LocalDate(2020, JANUARY, 1),
    start: YearMonth = YearMonth(2020, JANUARY),
    end: YearMonth = YearMonth(2020, MARCH),
  ) = crossoverRange(mode?.let { TimeFrame(start, end, it) }, today, earliest)

  @Test
  fun `No time frame covers all data up to last month`() {
    val range = range(mode = null, earliest = LocalDate(2024, MARCH, 10))
    assertThat(range).isEqualTo(YearMonth(2024, MARCH)..YearMonth(2026, APRIL))
  }

  @Test
  fun `Full ignores the stored range`() {
    val range = range(Full, earliest = LocalDate(2024, MARCH, 10))
    assertThat(range).isEqualTo(YearMonth(2024, MARCH)..YearMonth(2026, APRIL))
  }

  @Test
  fun `Full with no transactions is just last month`() {
    val range = range(Full, earliest = null)
    assertThat(range).isEqualTo(YearMonth(2026, APRIL)..YearMonth(2026, APRIL))
  }

  @Test
  fun `Earliest transaction this month is clamped to last month`() {
    val range = range(Full, earliest = LocalDate(2026, MAY, 2))
    assertThat(range).isEqualTo(YearMonth(2026, APRIL)..YearMonth(2026, APRIL))
  }

  @Test
  fun `Sliding window is shifted back one month`() {
    assertThat(range(SlidingWindow)).isEqualTo(YearMonth(2026, FEBRUARY)..YearMonth(2026, APRIL))
  }

  @Test
  fun `Unknown behaves like a sliding window`() {
    assertThat(range(Unknown)).isEqualTo(YearMonth(2026, FEBRUARY)..YearMonth(2026, APRIL))
  }

  @Test
  fun `Sliding window is clamped to the earliest month`() {
    val range = range(SlidingWindow, earliest = LocalDate(2026, MARCH, 20))
    assertThat(range).isEqualTo(YearMonth(2026, MARCH)..YearMonth(2026, APRIL))
  }

  @Test
  fun `Year to date is not shifted but ends last month`() {
    assertThat(range(YearToDate)).isEqualTo(YearMonth(2026, JANUARY)..YearMonth(2026, APRIL))
  }

  @Test
  fun `Last month is not shifted`() {
    assertThat(range(LastMonth)).isEqualTo(YearMonth(2026, APRIL)..YearMonth(2026, APRIL))
  }

  @Test
  fun `Static range past last month is clamped to last month`() {
    val range = range(Static, start = YearMonth(2026, JANUARY), end = YearMonth(2026, DECEMBER))
    assertThat(range).isEqualTo(YearMonth(2026, JANUARY)..YearMonth(2026, APRIL))
  }

  @Test
  fun `Static range before the earliest month collapses to the earliest month`() {
    val range = range(Static, earliest = LocalDate(2024, JUNE, 1))
    assertThat(range).isEqualTo(YearMonth(2024, JUNE)..YearMonth(2024, JUNE))
  }
}
