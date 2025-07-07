package actual.core.model

import kotlin.test.Test
import kotlin.test.assertEquals

class PercentTest {
  @Test
  fun `To string with decimals`() {
    assertEquals(expected = "12.34%", actual = 12.344.percent.toString(decimalPlaces = 2))
    assertEquals(expected = "12.35%", actual = 12.346.percent.toString(decimalPlaces = 2))
    assertEquals(expected = "123.40%", actual = 123.4.percent.toString(decimalPlaces = 2))
    assertEquals(expected = "123%", actual = 123.4.percent.toString())
  }
}
