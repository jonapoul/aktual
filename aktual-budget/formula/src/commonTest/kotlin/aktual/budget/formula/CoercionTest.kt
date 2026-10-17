package aktual.budget.formula

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import kotlin.test.Test

// Expected values come from HyperFormula with upstream's config
class CoercionTest {
  @Test
  fun `Text becomes a number when it looks like one`() {
    assertThat(textToNumber("")).isEqualTo(0.0)
    assertThat(textToNumber("  5  ")).isEqualTo(5.0)
    assertThat(textToNumber("+5")).isEqualTo(5.0)
    assertThat(textToNumber("1e3")).isEqualTo(1000.0)
    assertThat(textToNumber("1,000")).isNull()
    assertThat(textToNumber("abc")).isNull()
  }

  @Test
  fun `Percentages and dollar amounts become numbers`() {
    assertThat(textToNumber("5%")).isEqualTo(0.05)
    assertThat(textToNumber("5 %")).isEqualTo(0.05)
    assertThat(textToNumber("$5")).isEqualTo(5.0)
    assertThat(textToNumber("5$")).isEqualTo(5.0)
  }

  @Test
  fun `Dates become serial numbers`() {
    assertThat(dateStringToNumber("2025-01-31")).isEqualTo(45688.0)
    assertThat(dateStringToNumber("31/01/2025")).isEqualTo(45688.0)
    assertThat(dateStringToNumber("2025/01/31")).isEqualTo(45688.0)
    assertThat(dateStringToNumber("29/02/2024")).isEqualTo(45351.0)
    assertThat(dateStringToNumber("1899-12-30")).isEqualTo(0.0)
  }

  @Test
  fun `Invalid dates aren't numbers`() {
    assertThat(dateStringToNumber("29/02/2023")).isNull()
    assertThat(dateStringToNumber("1/2/25")).isNull()
    assertThat(dateStringToNumber("2025-13-01")).isNull()
    assertThat(dateStringToNumber("1500-01-01")).isNull()
    assertThat(dateStringToNumber("11")).isNull()
  }

  @Test
  fun `Times become fractions of a day`() {
    assertThat(dateStringToNumber("10:30")).isEqualTo(0.4375)
    assertThat(dateStringToNumber("10:30 pm")).isEqualTo(0.9375)
    assertThat(dateStringToNumber("2025-01-31 10:30")).isEqualTo(45688.4375)
    assertThat(dateStringToNumber("10:30:15.5")).isNotNull()
    assertThat(dateStringToNumber("13:00 pm")).isNull()
  }

  @Test
  fun `Numbers become text like JavaScript`() {
    assertThat(jsNumberToString(0.0)).isEqualTo("0")
    assertThat(jsNumberToString(100.0)).isEqualTo("100")
    assertThat(jsNumberToString(-2.5)).isEqualTo("-2.5")
    assertThat(jsNumberToString(0.001)).isEqualTo("0.001")
    assertThat(jsNumberToString(0.000001)).isEqualTo("0.000001")
    assertThat(jsNumberToString(1.5e-7)).isEqualTo("1.5e-7")
    assertThat(jsNumberToString(1e20)).isEqualTo("100000000000000000000")
    assertThat(jsNumberToString(1e21)).isEqualTo("1e+21")
    assertThat(jsNumberToString(1.2345e25)).isEqualTo("1.2345e+25")
    assertThat(jsNumberToString(123456789012345678901.0)).isEqualTo("123456789012345680000")
  }

  @Test
  fun `Rounding for output keeps 10 significant digits`() {
    assertThat(roundForOutput(0.0)).isEqualTo(0.0)
    assertThat(roundForOutput(1.0 / 3)).isEqualTo(0.33333333333)
    assertThat(roundForOutput(2.0 / 3)).isEqualTo(0.66666666667)
    assertThat(roundForOutput(-2.0 / 3)).isEqualTo(-0.66666666667)
    assertThat(roundForOutput(12345.678)).isEqualTo(12345.678)
  }
}
