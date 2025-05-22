@file:Suppress("JUnitMalformedDeclaration")

package actual.budget.model

import app.cash.burst.Burst
import app.cash.burst.burstValues
import dev.drewhamilton.poko.Poko
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

@Burst
class TimestampTest(
  private val case: TestCase = burstValues(TEST_CASE_1, TEST_CASE_2),
) {
  @Test
  fun `Parse and stringify`() = runTest {
    val parsed = Timestamp.parse(case.string)
    assertEquals(expected = case.timestamp, actual = parsed)
    val stringified = parsed.toString()
    assertEquals(expected = case.string, actual = stringified)
  }

  @Poko
  class TestCase(
    val string: String,
    val timestamp: Timestamp,
  )

  companion object {
    val TEST_CASE_1 = TestCase(
      string = "2024-03-16T18:14:09.237Z-000B-889aaebae6298282",
      timestamp = Timestamp(
        instant = Instant.parse("2024-03-16T18:14:09.237Z"),
        counter = 11,
        node = "889aaebae6298282",
      ),
    )

    val TEST_CASE_2 = TestCase(
      string = "9999-12-31T23:59:59.999Z-FFFF-FFFFFFFFFFFFFFFF",
      timestamp = Timestamp.MAXIMUM,
    )
  }
}
