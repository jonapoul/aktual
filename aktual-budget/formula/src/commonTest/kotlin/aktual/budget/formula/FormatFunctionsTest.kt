package aktual.budget.formula

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

// Expected values come from running the same formulas through HyperFormula with upstream's config
// and a copy of upstream's custom functions
class FormatFunctionsTest {
  @Test
  fun `INTEGER_TO_AMOUNT divides by a power of ten`() = runTest {
    assertThat(evaluate("=INTEGER_TO_AMOUNT(12345)")).isEqualTo(NumberValue(123.45))
    assertThat(evaluate("=INTEGER_TO_AMOUNT(12345,0)")).isEqualTo(NumberValue(12345.0))
    assertThat(evaluate("=INTEGER_TO_AMOUNT(1,)")).isEqualTo(NumberValue(1.0))
    assertThat(evaluate("=INTEGER_TO_AMOUNT(1,0.5)")).isEqualTo(NumberValue(0.31622776602))
    assertThat(evaluate("=INTEGER_TO_AMOUNT(5,400)")).isEqualTo(NumberValue(0.0))
    assertThat(evaluate("=INTEGER_TO_AMOUNT(5,-400)")).isError(Num, NotFinite)
  }

  @Test
  fun `FIXED rounds the binary value half up`() = runTest {
    assertThat(evaluate("=FIXED(2.5)")).isEqualTo(TextValue("3"))
    assertThat(evaluate("=FIXED(1.005,2)")).isEqualTo(TextValue("1.00"))
    assertThat(evaluate("=FIXED(123456789.125,2)")).isEqualTo(TextValue("123456789.13"))
    assertThat(evaluate("=FIXED(0.1+0.2,20)")).isEqualTo(TextValue("0.30000000000000004441"))
    assertThat(evaluate("=FIXED(-0.001,2)")).isEqualTo(TextValue("-0.00"))
    assertThat(evaluate("=FIXED(1e21,2)")).isEqualTo(TextValue("1e+21"))
    assertThat(evaluate("=FIXED(-1e21,2)")).isEqualTo(TextValue("-1e+21"))
  }

  @Test
  fun `FIXED truncates the decimals`() = runTest {
    assertThat(evaluate("=FIXED(1,1.7)")).isEqualTo(TextValue("1.0"))
    assertThat(evaluate("=FIXED(1,-0.5)")).isEqualTo(TextValue("1"))
    assertThat(evaluate("=FIXED(1,)")).isEqualTo(TextValue("1"))
    assertThat(evaluate("=FIXED(,2)")).isEqualTo(TextValue("0.00"))
    assertThat(evaluate("=FIXED(1,100.9)")).isEqualTo(TextValue("1." + "0".repeat(100)))
  }

  @Test
  fun `Decimals outside 0 to 100 are errors`() = runTest {
    assertThat(evaluate("=FIXED(1,-1)")).isError(Value, DecimalsOutOfRange)
    assertThat(evaluate("=FIXED(1,101)")).isError(Value, DecimalsOutOfRange)
    assertThat(evaluate("=FORMATNUMBER(1,101)")).isError(Value, DecimalsOutOfRange)
    assertThat(evaluate("=FORMATCURRENCY(1,\"$\",-1)")).isError(Value, DecimalsOutOfRange)
  }

  @Test
  fun `Arguments are coerced`() = runTest {
    assertThat(evaluate("""=FIXED("abc")""")).isError(Value, NumberCoercion)
    assertThat(evaluate("=FIXED(1/0)")).isError(DivByZero)
    assertThat(evaluate("=FORMATNUMBER(1234,2,5)")).isEqualTo(TextValue("15234.00"))
    assertThat(evaluate("""=FORMATNUMBER("1,234.5")""")).isError(Value, NumberCoercion)
    assertThat(evaluate("""=FORMATNUMBER(1,2,",",".",1)""")).isError(NotAvailable, WrongArgCount)
  }

  @Test
  fun `FORMATNUMBER groups thousands`() = runTest {
    assertThat(evaluate("=FORMATNUMBER(1234567.891)")).isEqualTo(TextValue("1,234,567.89"))
    assertThat(evaluate("=FORMATNUMBER(1234567.891,0)")).isEqualTo(TextValue("1,234,568"))
    assertThat(evaluate("""=FORMATNUMBER(1234567.891,1,".",",")"""))
      .isEqualTo(TextValue("1.234.567,9"))
    assertThat(evaluate("""=FORMATNUMBER(-1234.5,2," ")""")).isEqualTo(TextValue("-1 234.50"))
    assertThat(evaluate("""=FORMATNUMBER(1234.5,2,"ab","cd")""")).isEqualTo(TextValue("1ab234cd50"))
    assertThat(evaluate("=FORMATNUMBER(-0.001)")).isEqualTo(TextValue("-0.00"))
    assertThat(evaluate("=FORMATNUMBER(1.5e21,2)")).isEqualTo(TextValue("1.5e+21"))
  }

  @Test
  fun `FORMATNUMBER empty arguments are given`() = runTest {
    assertThat(evaluate("=FORMATNUMBER(1234.5,)")).isEqualTo(TextValue("1,235"))
    assertThat(evaluate("=FORMATNUMBER(1234.5,2,)")).isEqualTo(TextValue("1234.50"))
    assertThat(evaluate("""=FORMATNUMBER(1234.5,2,",",)""")).isEqualTo(TextValue("1,23450"))
    assertThat(evaluate("=FORMATNUMBER(1234.5,0.5)")).isEqualTo(TextValue("1,235"))
    assertThat(evaluate("=FORMATNUMBER(1234.5,1.5)")).isEqualTo(TextValue("1,234.5"))
  }

  @Test
  fun `Thousands separators expand JavaScript replacement patterns`() = runTest {
    assertThat(evaluate("""=FORMATNUMBER(1234567,0,"$&")""")).isEqualTo(TextValue("1234567"))
    assertThat(evaluate("""=FORMATNUMBER(1234567,0,"$")""")).isEqualTo(TextValue("1$234$567"))
    assertThat(evaluate("""=FORMATNUMBER(-1234567,0,"$$")""")).isEqualTo(TextValue("-1$234$567"))
    assertThat(evaluate("""=FORMATNUMBER(1234567,0,"$1")""")).isEqualTo(TextValue("1567234567567"))
    assertThat(evaluate("""=FORMATNUMBER(1234567,0,"$'")"""))
      .isEqualTo(TextValue("1234567234567567"))
    assertThat(evaluate("""=FORMATNUMBER(1234567,0,"[$01|$10|$0|$2|$`]")"""))
      .isEqualTo(TextValue("1[567|5670|$0|$2|1]234[567|5670|$0|$2|1234]567"))
  }

  @Test
  fun `FORMATCURRENCY defaults to dollars`() = runTest {
    assertThat(evaluate("=FORMATCURRENCY(1234.5)")).isEqualTo(TextValue("$1,234.50"))
    assertThat(evaluate("""=FORMATCURRENCY(-1234.567,"kr",3,".",",","after",TRUE())"""))
      .isEqualTo(TextValue("-1.234,567${NARROW_NO_BREAK_SPACE}kr"))
    assertThat(evaluate("""=FORMATCURRENCY(1,"$",2,",",".","before",FALSE(),1)"""))
      .isError(NotAvailable, WrongArgCount)
  }

  @Test
  fun `Left out arguments use the format`() = runTest {
    assertThat(evaluateEuros("=FORMATNUMBER(1234.5)")).isEqualTo(TextValue("1.235"))
    assertThat(evaluateEuros("=FORMATNUMBER(1234.5,2)")).isEqualTo(TextValue("1.234,50"))
    assertThat(evaluateEuros("=FORMATCURRENCY(1234.5)"))
      .isEqualTo(TextValue("1.234,50$NARROW_NO_BREAK_SPACE€"))
    assertThat(evaluateEuros("=FORMATCURRENCY(-1234.5)"))
      .isEqualTo(TextValue("-1.234,50$NARROW_NO_BREAK_SPACE€"))
    assertThat(evaluateEuros("=FORMATCURRENCY(-0.001)"))
      .isEqualTo(TextValue("-0,00$NARROW_NO_BREAK_SPACE€"))
    assertThat(evaluateEuros("=FORMATCURRENCY(1e21)"))
      .isEqualTo(TextValue("1e+21$NARROW_NO_BREAK_SPACE€"))
    assertThat(evaluateEuros("""=FORMATCURRENCY(1234.5,"£")"""))
      .isEqualTo(TextValue("1.234,50$NARROW_NO_BREAK_SPACE£"))
    assertThat(evaluateEuros("""=FORMATCURRENCY(1234.5,"£",0)"""))
      .isEqualTo(TextValue("1.235$NARROW_NO_BREAK_SPACE£"))
  }

  @Test
  fun `FORMATCURRENCY empty arguments are given`() = runTest {
    assertThat(evaluateEuros("=FORMATCURRENCY(1234.5,)"))
      .isEqualTo(TextValue("1.234,50$NARROW_NO_BREAK_SPACE"))
    assertThat(evaluateEuros("""=FORMATCURRENCY(1234.5,"£",2,",",".","after",)"""))
      .isEqualTo(TextValue("1,234.50£"))
  }

  @Test
  fun `FORMATCURRENCY symbol positions must match exactly`() = runTest {
    assertThat(evaluateEuros("""=FORMATCURRENCY(1234.5,"£",2,",",".","before",FALSE())"""))
      .isEqualTo(TextValue("£1,234.50"))
    assertThat(evaluateEuros("""=FORMATCURRENCY(1234.5,"£",2,",",".","AFTER")"""))
      .isEqualTo(TextValue("1,234.50$NARROW_NO_BREAK_SPACE£"))
    assertThat(evaluateEuros("""=FORMATCURRENCY(1234.5,"£",2,",",".",)"""))
      .isEqualTo(TextValue("1,234.50$NARROW_NO_BREAK_SPACE£"))
  }

  @Test
  fun `FORMATCURRENCY space must be a boolean`() = runTest {
    assertThat(evaluateEuros("""=FORMATCURRENCY(1234.5,"£",2,",",".","after",1)"""))
      .isEqualTo(TextValue("1,234.50$NARROW_NO_BREAK_SPACE£"))
    assertThat(evaluateEuros("""=FORMATCURRENCY(1234.5,"£",2,",",".","after","x")"""))
      .isError(Value, WrongArgType)
  }

  private suspend fun evaluateEuros(formula: String): FormulaValue =
    evaluateWithWarnings(formula, format = EUROS).value

  private companion object {
    val EUROS =
      FormulaFormat(
        decimalPlaces = 0,
        thousandsSeparator = ".",
        decimalSeparator = ",",
        currencySymbol = "€",
        currencyDecimalPlaces = 2,
        symbolAfterAmount = true,
        spaceBetweenAmountAndSymbol = true,
      )
  }
}
