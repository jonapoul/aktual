@file:Suppress("JUnitMalformedDeclaration")
package actual.budget.model

import app.cash.burst.Burst
import app.cash.burst.burstValues
import dev.drewhamilton.poko.Poko
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlin.test.Test
import kotlin.test.assertEquals

@Burst
class ParseDateTest(
  private val case: TestCase = burstValues(
    TestCase(year = 2025, month = Month.JULY, day = 27, expected = "2025-07-27"),
    TestCase(year = 1, month = Month.JANUARY, day = 1, expected = "0001-01-01"),
    TestCase(year = 9999, month = Month.DECEMBER, day = 31, expected = "9999-12-31"),
  ),
) {
  @Poko class TestCase(val year: Int, val month: Month, val day: Int, val expected: String)

  @Test
  fun `Parse and stringify`() {
    val date = ReportDate.Date(LocalDate(case.year, case.month, case.day))
    assertEquals(expected = case.expected, actual = date.toString())
    assertEquals(expected = date, actual = ReportDate.parse(case.expected))
  }
}
