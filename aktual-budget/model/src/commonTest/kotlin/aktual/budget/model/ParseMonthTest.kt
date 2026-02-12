package aktual.budget.model

import app.cash.burst.Burst
import app.cash.burst.burstValues
import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth

@Burst
class ParseMonthTest(
  private val case: TestCase =
    burstValues(
      TestCase(year = 2025, month = Month.JULY, expected = "2025-07"),
      TestCase(year = 1, month = Month.JANUARY, expected = "0001-01"),
      TestCase(year = 9999, month = Month.DECEMBER, expected = "9999-12"),
    )
) {
  data class TestCase(val year: Int, val month: Month, val expected: String)

  @Test
  fun `Parse and stringify`() {
    val date = ReportDate.Month(YearMonth(case.year, case.month))
    assertThat(date.toString()).isEqualTo(case.expected)
    assertThat(ReportDate.parse(case.expected)).isEqualTo(date)
  }
}
