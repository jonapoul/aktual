package aktual.budget.reports.vm

import aktual.budget.model.DateRangeType
import aktual.budget.model.FirstDayOfWeek
import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateRange

// Cases with a "today" of 2017-01-01 are ported from upstream's getLiveRange.test.ts
class CustomReportRangeTest {
  @Test
  fun `Last 30 days ends today`() {
    val range = range(Last30Days, today = UPSTREAM_TODAY)
    assertThat(range).isEqualTo(date("2016-12-03")..date("2017-01-01"))
  }

  @Test
  fun `Last 30 days ignores include current`() {
    val excluded = range(Last30Days, today = UPSTREAM_TODAY, includeCurrent = false)
    val included = range(Last30Days, today = UPSTREAM_TODAY, includeCurrent = true)
    assertThat(excluded).isEqualTo(included)
  }

  @Test
  fun `Last 30 days is clamped to the earliest transaction`() {
    val range = range(Last30Days, today = UPSTREAM_TODAY, earliest = date("2016-12-20"))
    assertThat(range).isEqualTo(date("2016-12-20")..date("2017-01-01"))
  }

  @Test
  fun `Last 30 days ends today even when the latest transaction is earlier`() {
    val range = range(Last30Days, today = UPSTREAM_TODAY, latest = date("2016-12-25"))
    assertThat(range).isEqualTo(date("2016-12-03")..date("2017-01-01"))
  }

  @Test
  fun `Year to date ends today even when the latest transaction is earlier`() {
    val range = range(YearToDate, today = UPSTREAM_TODAY, latest = date("2016-12-25"))
    assertThat(range).isEqualTo(date("2017-01-01")..date("2017-01-01"))
  }

  @Test
  fun `Current quarter covers the whole quarter`() {
    val range = range(CurrentQuarter, today = UPSTREAM_TODAY)
    assertThat(range).isEqualTo(date("2017-01-01")..date("2017-03-31"))
  }

  @Test
  fun `Current quarter is clamped to the earliest transaction`() {
    val range = range(CurrentQuarter, today = UPSTREAM_TODAY, earliest = date("2017-02-15"))
    assertThat(range).isEqualTo(date("2017-02-15")..date("2017-03-31"))
  }

  @Test
  fun `Previous quarter crosses into the previous year`() {
    val range = range(PreviousQuarter, today = UPSTREAM_TODAY)
    assertThat(range).isEqualTo(date("2016-10-01")..date("2016-12-31"))
  }

  @Test
  fun `This month covers the whole month`() {
    assertThat(range(ThisMonth)).isEqualTo(date("2026-05-01")..date("2026-05-31"))
  }

  @Test
  fun `Last month includes the current month when asked`() {
    assertThat(range(LastMonth, includeCurrent = true))
      .isEqualTo(date("2026-04-01")..date("2026-05-31"))
    assertThat(range(LastMonth, includeCurrent = false))
      .isEqualTo(date("2026-04-01")..date("2026-04-30"))
  }

  @Test
  fun `Last 3 months includes the current month when asked`() {
    assertThat(range(Last3Months, includeCurrent = true))
      .isEqualTo(date("2026-02-01")..date("2026-05-31"))
    assertThat(range(Last3Months, includeCurrent = false))
      .isEqualTo(date("2026-02-01")..date("2026-04-30"))
  }

  @Test
  fun `Last 12 months is not clamped to the earliest transaction`() {
    val range = range(Last12Months, includeCurrent = false, earliest = date("2026-01-10"))
    assertThat(range).isEqualTo(date("2025-05-01")..date("2026-04-30"))
  }

  @Test
  fun `This week starts on the first day of the week`() {
    // TODAY is a Friday
    assertThat(range(ThisWeek, firstDayOfWeek = Sunday))
      .isEqualTo(date("2026-05-10")..date("2026-05-16"))
    assertThat(range(ThisWeek, firstDayOfWeek = Monday))
      .isEqualTo(date("2026-05-11")..date("2026-05-17"))
    assertThat(range(ThisWeek, firstDayOfWeek = Friday))
      .isEqualTo(date("2026-05-15")..date("2026-05-21"))
  }

  @Test
  fun `Last week includes the current week when asked`() {
    assertThat(range(LastWeek, includeCurrent = true, firstDayOfWeek = Monday))
      .isEqualTo(date("2026-05-04")..date("2026-05-17"))
    assertThat(range(LastWeek, includeCurrent = false, firstDayOfWeek = Monday))
      .isEqualTo(date("2026-05-04")..date("2026-05-10"))
  }

  @Test
  fun `Last year covers the whole previous year`() {
    assertThat(range(LastYear)).isEqualTo(date("2025-01-01")..date("2025-12-31"))
  }

  @Test
  fun `Prior year to date ends a year before today`() {
    assertThat(range(PriorYearToDate)).isEqualTo(date("2025-01-01")..date("2025-05-15"))
  }

  @Test
  fun `Prior year to date from a leap day ends on the 28th`() {
    val range = range(PriorYearToDate, today = date("2024-02-29"))
    assertThat(range).isEqualTo(date("2023-01-01")..date("2023-02-28"))
  }

  @Test
  fun `All time covers every transaction`() {
    val range = range(AllTime, earliest = date("2021-03-04"), latest = date("2026-06-01"))
    assertThat(range).isEqualTo(date("2021-03-04")..date("2026-06-01"))
  }

  @Test
  fun `Static dates are used as they are`() {
    val dates = CustomReportDates.Static(date("2011-09-01"), date("2025-07-31"))
    val range = resolveCustomReportRange(dates, TODAY, EARLIEST, TODAY, Sunday)
    assertThat(range).isEqualTo(date("2011-09-01")..date("2025-07-31"))
  }

  private fun range(
    type: DateRangeType,
    includeCurrent: Boolean = true,
    today: LocalDate = TODAY,
    earliest: LocalDate = EARLIEST,
    latest: LocalDate = today,
    firstDayOfWeek: FirstDayOfWeek = Sunday,
  ): LocalDateRange =
    resolveCustomReportRange(
      dates = CustomReportDates.Live(type, includeCurrent),
      today = today,
      earliestTransaction = earliest,
      latestTransaction = latest,
      firstDayOfWeek = firstDayOfWeek,
    )

  private companion object {
    val TODAY = date("2026-05-15")
    val EARLIEST = date("2015-01-01")
    val UPSTREAM_TODAY = date("2017-01-01")

    fun date(string: String) = LocalDate.parse(string)
  }
}
