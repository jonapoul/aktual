package aktual.budget.reports.vm

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month.APRIL
import kotlinx.datetime.Month.DECEMBER
import kotlinx.datetime.Month.FEBRUARY
import kotlinx.datetime.Month.JANUARY
import kotlinx.datetime.Month.JULY
import kotlinx.datetime.Month.JUNE
import kotlinx.datetime.Month.MARCH
import kotlinx.datetime.Month.MAY
import kotlinx.datetime.Month.OCTOBER
import kotlinx.datetime.YearMonth

class TimeRangeTest {
  private val today = LocalDate(2026, MAY, 15)

  private fun resolve(
    mode: TimeFrameMode,
    start: YearMonth = YearMonth(2020, JANUARY),
    end: YearMonth = YearMonth(2020, MARCH),
    latestTransaction: LocalDate? = null,
  ) =
    resolveTimeRange(
      timeFrame = TimeFrame(start, end, mode),
      default = null,
      today = today,
      latestTransaction = latestTransaction,
    )

  @Test
  fun `No time frame defaults to the last six months`() {
    val range = resolveTimeRange(null, default = null, today, latestTransaction = null)
    assertThat(range).isEqualTo(YearMonth(2025, DECEMBER)..YearMonth(2026, MAY))
  }

  @Test
  fun `No time frame uses the default when given`() {
    val current = YearMonth(2026, MAY)
    val default = TimeFrame(current, current, SlidingWindow)
    val range = resolveTimeRange(null, default, today, latestTransaction = null)
    assertThat(range).isEqualTo(current..current)
  }

  @Test
  fun `Sliding window keeps its width and ends this month`() {
    assertThat(resolve(SlidingWindow)).isEqualTo(YearMonth(2026, MARCH)..YearMonth(2026, MAY))
  }

  @Test
  fun `Static keeps the stored range`() {
    assertThat(resolve(Static)).isEqualTo(YearMonth(2020, JANUARY)..YearMonth(2020, MARCH))
  }

  @Test
  fun `Full ends this month when there are no future transactions`() {
    val range = resolve(Full, latestTransaction = LocalDate(2026, FEBRUARY, 1))
    assertThat(range).isEqualTo(YearMonth(2020, JANUARY)..YearMonth(2026, MAY))
  }

  @Test
  fun `Full extends to the latest future transaction`() {
    val range = resolve(Full, latestTransaction = LocalDate(2026, JULY, 3))
    assertThat(range).isEqualTo(YearMonth(2020, JANUARY)..YearMonth(2026, JULY))
  }

  @Test
  fun `Last month`() {
    assertThat(resolve(LastMonth)).isEqualTo(YearMonth(2026, APRIL)..YearMonth(2026, APRIL))
  }

  @Test
  fun `Last year`() {
    assertThat(resolve(LastYear)).isEqualTo(YearMonth(2025, JANUARY)..YearMonth(2025, DECEMBER))
  }

  @Test
  fun `Year to date`() {
    assertThat(resolve(YearToDate)).isEqualTo(YearMonth(2026, JANUARY)..YearMonth(2026, MAY))
  }

  @Test
  fun `Prior year to date`() {
    assertThat(resolve(PriorYearToDate)).isEqualTo(YearMonth(2025, JANUARY)..YearMonth(2025, MAY))
  }

  @Test
  fun `Current quarter`() {
    assertThat(resolve(CurrentQuarter)).isEqualTo(YearMonth(2026, APRIL)..YearMonth(2026, JUNE))
  }

  @Test
  fun `Previous quarter`() {
    assertThat(resolve(PreviousQuarter)).isEqualTo(YearMonth(2026, JANUARY)..YearMonth(2026, MARCH))
  }

  @Test
  fun `Previous quarter wraps into last year`() {
    val range =
      resolveTimeRange(
        TimeFrame(YearMonth(2020, JANUARY), YearMonth(2020, JANUARY), PreviousQuarter),
        default = null,
        today = LocalDate(2026, FEBRUARY, 1),
        latestTransaction = null,
      )
    assertThat(range).isEqualTo(YearMonth(2025, OCTOBER)..YearMonth(2025, DECEMBER))
  }
}
