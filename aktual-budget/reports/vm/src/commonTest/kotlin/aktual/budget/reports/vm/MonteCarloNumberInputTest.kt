package aktual.budget.reports.vm

import aktual.budget.reports.vm.NumberInput.Valid
import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class MonteCarloNumberInputTest {
  @Test
  fun `Values show without trailing zeros`() {
    assertThat(numberInputText(null)).isEqualTo("")
    assertThat(numberInputText(60.0)).isEqualTo("60")
    assertThat(numberInputText(0.065, scale = 100)).isEqualTo("6.5")
    assertThat(numberInputText(0.07, scale = 100)).isEqualTo("7")
    assertThat(numberInputText(-0.125, scale = 100)).isEqualTo("-12.5")
    assertThat(numberInputText(0.0015, scale = 100)).isEqualTo("0.15")
  }

  @Test
  fun `Percentages are read back as fractions`() {
    assertThat(parse("6.5", scale = 100)).isEqualTo(Valid(0.065))
    assertThat(parse("6,5", scale = 100)).isEqualTo(Valid(0.065))
  }

  @Test
  fun `Values are clamped and rounded`() {
    assertThat(parse("150")).isEqualTo(Valid(100.0))
    assertThat(parse("-3")).isEqualTo(Valid(0.0))
    assertThat(parse("61.6", roundToInteger = true)).isEqualTo(Valid(62.0))
  }

  @Test
  fun `Only optional fields can be cleared`() {
    assertThat(parse(" ", allowEmpty = true)).isEqualTo(Valid(null))
    assertThat(parse(" ")).isEqualTo(Invalid)
  }

  @Test
  fun `Text that isn't a number is rejected`() {
    assertThat(parse("abc")).isEqualTo(Invalid)
    assertThat(parse("NaN")).isEqualTo(Invalid)
    assertThat(parse("1f")).isEqualTo(Invalid)
  }

  private fun parse(
    text: String,
    scale: Int = 1,
    allowEmpty: Boolean = false,
    roundToInteger: Boolean = false,
  ) =
    parseNumberInput(
      text = text,
      min = 0.0,
      max = 100.0,
      scale = scale,
      allowEmpty = allowEmpty,
      roundToInteger = roundToInteger,
    )
}
