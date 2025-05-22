@file:Suppress("JUnitMalformedDeclaration")

package actual.budget.model

import app.cash.burst.Burst
import app.cash.burst.burstValues
import dev.drewhamilton.poko.Poko
import kotlinx.datetime.Month
import org.junit.Test
import kotlin.test.assertEquals

@Burst
class ParseMonthTest(
  private val case: TestCase = burstValues(
    TestCase(year = 2025, month = Month.JULY, expected = "2025-07"),
    TestCase(year = 1, month = Month.JANUARY, expected = "0001-01"),
    TestCase(year = 9999, month = Month.DECEMBER, expected = "9999-12"),
  ),
) {
  @Poko class TestCase(val year: Int, val month: Month, val expected: String)

  @Test
  fun `Parse and stringify`() {
    val date = ReportDate.Month(YearAndMonth(case.year, case.month))
    assertEquals(expected = case.expected, actual = date.toString())
    assertEquals(expected = date, actual = ReportDate.parse(case.expected))
  }
}
