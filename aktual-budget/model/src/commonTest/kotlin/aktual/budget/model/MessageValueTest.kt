package aktual.budget.model

import aktual.budget.model.MessageValue.Companion.decode
import aktual.budget.model.MessageValue.Number
import aktual.budget.model.MessageValue.Real
import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class MessageValueTest {
  @Test
  fun `Whole numbers decode to Number`() {
    assertThat(decode("N:1")).isEqualTo(Number(1))
    assertThat(decode("N:-2500")).isEqualTo(Number(-2500))
  }

  @Test
  fun `Fractions decode to Real`() {
    assertThat(decode("N:51.5074")).isEqualTo(Real(51.5074))
    assertThat(decode("N:-0.1278")).isEqualTo(Real(-0.1278))
    assertThat(decode("N:1e-7")).isEqualTo(Real(1e-7))
    assertThat(decode("N:1e+21")).isEqualTo(Real(1e21))
  }

  @Test
  fun `Real encodes as JS would`() {
    assertThat(Real(51.5074).encode()).isEqualTo("N:51.5074")
    assertThat(Real(-0.1278).encode()).isEqualTo("N:-0.1278")
    assertThat(Real(52.0).encode()).isEqualTo("N:52")
    assertThat(Real(0.00001).encode()).isEqualTo("N:0.00001")
    assertThat(Real(1.5e-7).encode()).isEqualTo("N:1.5e-7")
    assertThat(Real(1e-7).encode()).isEqualTo("N:1e-7")
    assertThat(Real(1e21).encode()).isEqualTo("N:1e+21")
    assertThat(Real(1.2345e22).encode()).isEqualTo("N:1.2345e+22")
    assertThat(Real(123456789.123).encode()).isEqualTo("N:123456789.123")
  }

  @Test
  fun `Real survives a round trip`() {
    for (value in listOf("N:51.5074", "N:-179.999999", "N:0.000123", "N:1.5e-7")) {
      assertThat(decode(value).encode()).isEqualTo(value)
    }
  }
}
