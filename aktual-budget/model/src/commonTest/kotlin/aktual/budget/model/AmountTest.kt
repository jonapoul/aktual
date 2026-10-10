package aktual.budget.model

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class AmountTest {
  private val Double.amount
    get() = Amount(this)

  private fun Amount.toString(
    format: NumberFormat = CommaDot,
    hideFraction: Boolean = false,
    includeSign: Boolean = false,
    isPrivacyEnabled: Boolean = false,
  ) =
    toString(
      numberFormatConfig = NumberFormatConfig(format, hideFraction),
      currencyConfig = CurrencyConfig(None, BeforeAmount, false),
      includeSign = includeSign,
      isPrivacyEnabled = isPrivacyEnabled,
    )

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
    assertThat(123_456_789.0.amount.toString(format = DotComma)).isEqualTo("123.456.789,00")
  }

  @Test
  fun `Privacy mask is the same for every amount`() {
    assertThat(0.0.amount.toString(isPrivacyEnabled = true)).isEqualTo("•••••")
    assertThat(123.45.amount.toString(isPrivacyEnabled = true)).isEqualTo("•••••")
    assertThat((-1234.56).amount.toString(isPrivacyEnabled = true)).isEqualTo("•••••")
    assertThat(123_456_789.0.amount.toString(isPrivacyEnabled = true)).isEqualTo("•••••")
  }

  @Test
  fun `Privacy mask hides the sign`() {
    assertThat(123.45.amount.toString(includeSign = true, isPrivacyEnabled = true))
      .isEqualTo("•••••")
  }

  @Test
  fun `Privacy mask keeps the currency symbol`() {
    val amount = 123.45.amount
    assertThat(amount.toString(currency = CurrencyConfig(PoundSterling, BeforeAmount, false)))
      .isEqualTo("£•••••")
    assertThat(amount.toString(currency = CurrencyConfig(PoundSterling, AfterAmount, true)))
      .isEqualTo("••••• £")
  }

  private fun Amount.toString(currency: CurrencyConfig) =
    toString(
      numberFormatConfig = NumberFormatConfig(CommaDot, hideFraction = false),
      currencyConfig = currency,
      includeSign = false,
      isPrivacyEnabled = true,
    )

  private companion object {
    const val WEIRD_SPACE = '\u00A0'
  }
}
