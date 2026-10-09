package aktual.budget.model

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import kotlin.test.Test

class AmountInputTest {
  @Test
  fun `Blank input is zero`() {
    assertThat(parseAmountInput("")).isEqualTo(Amount(0L))
    assertThat(parseAmountInput("  ")).isEqualTo(Amount(0L))
  }

  @Test
  fun `Input reads the last separator as the decimal point`() {
    assertThat(parseAmountInput("1,200.50")).isEqualTo(Amount(120_050L))
    assertThat(parseAmountInput("1.200,50")).isEqualTo(Amount(120_050L))
  }

  @Test
  fun `Input without digits is rejected`() {
    assertThat(parseAmountInput("abc")).isNull()
  }

  @Test
  fun `Input text round-trips`() {
    assertThat(Amount(120_050L).toInputText()).isEqualTo("1200.50")
    assertThat(Amount(0L).toInputText()).isEqualTo("")
  }

  @Test
  fun `Arithmetic follows operator precedence`() {
    assertThat(evaluateAmountInput("100 + 20 × 3")).isEqualTo(Amount(16_000L))
    assertThat(evaluateAmountInput("100 - 20 / 4")).isEqualTo(Amount(9_500L))
    assertThat(evaluateAmountInput("1,200.50+0.50")).isEqualTo(Amount(120_100L))
  }

  @Test
  fun `Arithmetic rounds to the cent`() {
    assertThat(evaluateAmountInput("100 ÷ 3")).isEqualTo(Amount(3_333L))
    assertThat(evaluateAmountInput("200 ÷ 3")).isEqualTo(Amount(6_667L))
  }

  @Test
  fun `Arithmetic allows negatives`() {
    assertThat(evaluateAmountInput("-25")).isEqualTo(Amount(-2_500L))
    assertThat(evaluateAmountInput("10 − 25")).isEqualTo(Amount(-1_500L))
    assertThat(evaluateAmountInput("10 * -2")).isEqualTo(Amount(-2_000L))
  }

  @Test
  fun `Malformed arithmetic is rejected`() {
    assertThat(evaluateAmountInput("10 +")).isNull()
    assertThat(evaluateAmountInput("10 ÷ 0")).isNull()
    assertThat(evaluateAmountInput("10 x 2")).isNull()
    assertThat(evaluateAmountInput("× 2")).isNull()
  }

  @Test
  fun `Blank arithmetic is zero`() {
    assertThat(evaluateAmountInput(" ")).isEqualTo(Amount(0L))
  }

  @Test
  fun `Signed input text keeps the minus`() {
    assertThat(Amount(-120_050L).toSignedInputText()).isEqualTo("-1200.50")
    assertThat(Amount(120_050L).toSignedInputText()).isEqualTo("1200.50")
  }
}
