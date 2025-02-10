package actual.budget.model

import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.assertEquals

@RunWith(Parameterized::class)
class TimestampTest(private val case: TestCase) {
  @Test
  fun `Parse and stringify`() = runTest {
    val parsed = Timestamp.parse(case.string)
    assertEquals(expected = case.timestamp, actual = parsed)
    val stringified = parsed.toString()
    assertEquals(expected = case.string, actual = stringified)
  }

  data class TestCase(
    val string: String,
    val timestamp: Timestamp,
  )

  companion object {
    @JvmStatic
    @Parameterized.Parameters
    fun data() = listOf(
      TestCase(
        string = "2024-03-16T18:14:09.237Z-000B-889aaebae6298282",
        timestamp = Timestamp(
          instant = Instant.parse("2024-03-16T18:14:09.237Z"),
          counter = 11,
          node = "889aaebae6298282",
        ),
      ),
      TestCase(
        string = "9999-12-31T23:59:59.999Z-FFFF-FFFFFFFFFFFFFFFF",
        timestamp = Timestamp.MAXIMUM,
      ),
    )
  }
}
