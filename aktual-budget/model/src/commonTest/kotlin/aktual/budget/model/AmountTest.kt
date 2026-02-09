package aktual.budget.model

import aktual.budget.model.NumberFormat.ApostropheDot
import aktual.budget.model.NumberFormat.DotComma
import aktual.budget.model.NumberFormat.SpaceComma
import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class AmountTest {
  @Test
  fun `Hide fraction`() {
    assertThat(123.45.amount.toString(hideFraction = true)).isEqualTo("123")
    assertThat(1234.56.amount.toString(format = ApostropheDot, hideFraction = true))
        .isEqualTo("1’235")
  }

  @Test
  fun `Space comma`() {
    assertThat(123.0.amount.toString(format = SpaceComma)).isEqualTo("123,00")
    assertThat(123.45.amount.toString(format = SpaceComma)).isEqualTo("123,45")
    assertThat(1234.56.amount.toString(format = SpaceComma)).isEqualTo("1${WEIRD_SPACE}234,56")
  }

  @Test
  fun `Dot comma`() {
    assertThat(123.0.amount.toString(format = DotComma)).isEqualTo("123,00")
    assertThat(123.45.amount.toString(format = DotComma)).isEqualTo("123,45")
    assertThat(1234.56.amount.toString(format = DotComma)).isEqualTo("1.234,56")
    assertThat(123456789.0.amount.toString(format = DotComma)).isEqualTo("123.456.789,00")
  }

  // One dash per digit - not including commas/spaces/dots
  @Test
  fun `With privacy`() {
    assertThat(123.0.amount.toString(isPrivacyEnabled = true)).isEqualTo("~~~~~")
    assertThat(123.45.amount.toString(isPrivacyEnabled = true)).isEqualTo("~~~~~")
    assertThat(1234.56.amount.toString(isPrivacyEnabled = true)).isEqualTo("~~~~~~")
    assertThat(123456789.0.amount.toString(isPrivacyEnabled = true)).isEqualTo("~~~~~~~~~~~")
  }

  private val Double.amount
    get() = Amount(this)

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
