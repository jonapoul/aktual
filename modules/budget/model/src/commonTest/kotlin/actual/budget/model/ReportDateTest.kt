package actual.budget.model

import actual.budget.model.ReportDateTest.TestCase.ParseDate
import actual.budget.model.ReportDateTest.TestCase.ParseMonth
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.assertEquals

@RunWith(Parameterized::class)
class ReportDateTest(private val case: TestCase) {
  @Test
  fun `Parse and stringify`() = when (case) {
    is ParseDate -> {
      val date = ReportDate.Date(LocalDate(case.year, case.month, case.day))
      assertEquals(expected = case.expected, actual = date.toString())
      assertEquals(expected = date, actual = ReportDate.parse(case.expected))
    }

    is ParseMonth -> {
      val date = ReportDate.Month(YearAndMonth(case.year, case.month))
      assertEquals(expected = case.expected, actual = date.toString())
      assertEquals(expected = date, actual = ReportDate.parse(case.expected))
    }
  }

  sealed interface TestCase {
    data class ParseMonth(val year: Int, val month: Month, val expected: String) : TestCase
    data class ParseDate(val year: Int, val month: Month, val day: Int, val expected: String) : TestCase
  }

  companion object {
    @JvmStatic
    @Parameterized.Parameters
    fun data() = listOf(
      ParseDate(year = 2025, month = Month.JULY, day = 27, expected = "2025-07-27"),
      ParseDate(year = 1, month = Month.JANUARY, day = 1, expected = "0001-01-01"),
      ParseDate(year = 9999, month = Month.DECEMBER, day = 31, expected = "9999-12-31"),

      ParseMonth(year = 2025, month = Month.JULY, expected = "2025-07"),
      ParseMonth(year = 1, month = Month.JANUARY, expected = "0001-01"),
      ParseMonth(year = 9999, month = Month.DECEMBER, expected = "9999-12"),
    )
  }
}
