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
}
