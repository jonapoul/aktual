package aktual.budget.model

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlinx.datetime.LocalDate.Companion.parse

class RecurOccurrencesTest {
  @Test
  fun `Next date before the start is the start`() {
    assertThat(MONTHLY.nextDate(from = parse("2025-12-01"))).isEqualTo(parse("2026-01-15"))
  }

  @Test
  fun `Next date between occurrences is the following one`() {
    assertThat(MONTHLY.nextDate(from = parse("2026-02-16"))).isEqualTo(parse("2026-03-15"))
  }

  @Test
  fun `Next date on an occurrence is that day`() {
    assertThat(MONTHLY.nextDate(from = parse("2026-02-15"))).isEqualTo(parse("2026-02-15"))
  }

  @Test
  fun `Next date moves off the weekend`() {
    // 2026-03-15 is a Sunday
    val from = parse("2026-02-17")
    assertThat(MONTHLY.copy(skipWeekend = true, weekendSolveMode = After).nextDate(from))
      .isEqualTo(parse("2026-03-16"))
    assertThat(MONTHLY.copy(skipWeekend = true, weekendSolveMode = Before).nextDate(from))
      .isEqualTo(parse("2026-03-13"))
  }

  @Test
  fun `Next date keeps a weekend occurrence moved after the weekend onto the day`() {
    // 2016-12-31 is a Saturday, moved to Monday 2017-01-02
    val from = parse("2017-01-01")
    for (frequency in listOf(RecurFrequency.Weekly, RecurFrequency.Monthly)) {
      val config =
        RecurConfig(
          frequency = frequency,
          start = parse("2016-12-31"),
          skipWeekend = true,
          weekendSolveMode = After,
        )
      assertThat(config.nextDate(from)).isEqualTo(parse("2017-01-02"))
    }
  }

  @Test
  fun `Next date keeps a weekend occurrence moved after the weekend onto a later day`() {
    // 2021-05-22 is a Saturday, moved to Monday 2021-05-24
    val config =
      RecurConfig(
        frequency = Weekly,
        start = parse("2021-05-22"),
        skipWeekend = true,
        weekendSolveMode = After,
      )
    assertThat(config.nextDate(from = parse("2021-05-23"))).isEqualTo(parse("2021-05-24"))
    assertThat(config.nextDate(from = parse("2021-05-25"))).isEqualTo(parse("2021-05-31"))
  }

  @Test
  fun `Next date doesn't keep a weekend occurrence moved before the weekend`() {
    // 2021-05-22 is a Saturday, moved to Friday 2021-05-21
    val config =
      RecurConfig(
        frequency = Weekly,
        start = parse("2021-05-22"),
        skipWeekend = true,
        weekendSolveMode = Before,
      )
    assertThat(config.nextDate(from = parse("2021-05-23"))).isEqualTo(parse("2021-05-28"))
  }

  @Test
  fun `Next date after the last of a limited count is the last occurrence`() {
    val config = MONTHLY.copy(endMode = AfterNOccurrences, endOccurrences = 3)
    assertThat(config.nextDate(from = parse("2026-06-01"))).isEqualTo(parse("2026-03-15"))
  }

  @Test
  fun `Next date after the end date is the last occurrence`() {
    val config = MONTHLY.copy(endMode = OnDate, endDate = parse("2026-02-20"))
    assertThat(config.nextDate(from = parse("2026-06-01"))).isEqualTo(parse("2026-02-15"))
  }

  @Test
  fun `Next date of a sparse schedule looks past the first search horizon`() {
    val config = RecurConfig(frequency = Yearly, start = parse("2020-05-01"), interval = 10)
    assertThat(config.nextDate(from = parse("2026-01-01"))).isEqualTo(parse("2030-05-01"))
  }

  @Test
  fun `Upcoming dates skip months without the day`() {
    val config = RecurConfig(frequency = Monthly, start = parse("2026-01-31"))
    assertThat(config.upcomingDates(from = parse("2026-01-01"), count = 3))
      .containsExactly(parse("2026-01-31"), parse("2026-03-31"), parse("2026-05-31"))
  }

  @Test
  fun `Upcoming dates stop when a limited schedule runs out`() {
    val config = MONTHLY.copy(endMode = AfterNOccurrences, endOccurrences = 2)
    assertThat(config.upcomingDates(from = parse("2026-01-01"), count = 5))
      .containsExactly(parse("2026-01-15"), parse("2026-02-15"))
  }

  @Test
  fun `Upcoming dates move off the weekend`() {
    // 2026-01-03 is a Saturday
    val config =
      RecurConfig(
        frequency = Weekly,
        start = parse("2026-01-03"),
        skipWeekend = true,
        weekendSolveMode = After,
      )
    assertThat(config.upcomingDates(from = parse("2026-01-01"), count = 2))
      .containsExactly(parse("2026-01-05"), parse("2026-01-12"))
  }

  private companion object {
    val MONTHLY = RecurConfig(frequency = Monthly, start = parse("2026-01-15"))
  }
}
