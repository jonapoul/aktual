package actual.budget.model

import actual.budget.model.NumberFormat.ApostropheDot
import actual.budget.model.NumberFormat.DotComma
import actual.budget.model.NumberFormat.SpaceComma
import kotlin.test.Test
import kotlin.test.assertEquals

class AmountTest {
  @Test
  fun `Hide fraction`() {
    assertEquals(actual = 123.45.amount.toString(hideFraction = true), expected = "123")
    assertEquals(actual = 1234.56.amount.toString(format = ApostropheDot, hideFraction = true), expected = "1’235")
  }

  @Test
  fun `Space comma`() {
    assertEquals(actual = 123.0.amount.toString(format = SpaceComma), expected = "123,00")
    assertEquals(actual = 123.45.amount.toString(format = SpaceComma), expected = "123,45")
    assertEquals(actual = 1234.56.amount.toString(format = SpaceComma), expected = "1${WEIRD_SPACE}234,56")
  }

  @Test
  fun `Dot comma`() {
    assertEquals(actual = 123.0.amount.toString(format = DotComma), expected = "123,00")
    assertEquals(actual = 123.45.amount.toString(format = DotComma), expected = "123,45")
    assertEquals(actual = 1234.56.amount.toString(format = DotComma), expected = "1.234,56")
    assertEquals(actual = 123456789.0.amount.toString(format = DotComma), expected = "123.456.789,00")
  }

  // One dash per digit - not including commas/spaces/dots
  @Test
  fun `With privacy`() {
    assertEquals(actual = 123.0.amount.toString(isPrivacyEnabled = true), expected = "~~~~~")
    assertEquals(actual = 123.45.amount.toString(isPrivacyEnabled = true), expected = "~~~~~")
    assertEquals(actual = 1234.56.amount.toString(isPrivacyEnabled = true), expected = "~~~~~~")
    assertEquals(actual = 123456789.0.amount.toString(isPrivacyEnabled = true), expected = "~~~~~~~~~~~")
  }

  private val Double.amount get() = Amount(this)

  private fun Amount.toString(
    format: NumberFormat = NumberFormat.CommaDot,
    hideFraction: Boolean = false,
    includeSign: Boolean = false,
    isPrivacyEnabled: Boolean = false,
  ) = toString(NumberFormatConfig(format, hideFraction), includeSign, isPrivacyEnabled)

  private companion object {
    const val WEIRD_SPACE = '\u00A0'
  }
}
