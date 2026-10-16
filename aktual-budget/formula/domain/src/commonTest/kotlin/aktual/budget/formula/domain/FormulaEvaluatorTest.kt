package aktual.budget.formula.domain

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.prop
import assertk.assertions.startsWith
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

// Expected values come from running the same formulas through HyperFormula with upstream's config
class FormulaEvaluatorTest {
  @Test
  fun `Formulas must start with equals`() = runTest {
    assertThat(evaluate("1+1")).isError(Generic, "Formula must start with =")
    assertThat(evaluate(" =1")).isError(Generic)
  }

  @Test
  fun `Literals`() = runTest {
    assertThat(evaluate("=1.5")).isEqualTo(NumberValue(1.5))
    assertThat(evaluate("=1.")).isEqualTo(NumberValue(1.0))
    assertThat(evaluate("=.5")).isEqualTo(NumberValue(0.5))
    assertThat(evaluate("=1e3")).isEqualTo(NumberValue(1000.0))
    assertThat(evaluate("=\"abc\"")).isEqualTo(TextValue("abc"))
    assertThat(evaluate("=TRUE()")).isEqualTo(BooleanValue(true))
    assertThat(evaluate("=false()")).isEqualTo(BooleanValue(false))
    assertThat(evaluate("=#VALUE!")).isError(FormulaErrorType.Value)
    assertThat(evaluate("=#n/a")).isError(NotAvailable)
  }

  @Test
  fun `Strings keep backslash escapes and have no doubled quote escape`() = runTest {
    assertThat(evaluate("=\"a\\\"b\"")).isEqualTo(TextValue("a\\\"b"))
    assertThat(evaluate("=\"a\"\"b\"")).isError(Generic)
  }

  @Test
  fun `Bare TRUE is an unknown name`() = runTest {
    assertThat(evaluate("=TRUE")).isError(Name, "Named expression TRUE not recognized.")
  }

  @Test
  fun `Operator precedence`() = runTest {
    assertThat(evaluate("=1+2*3")).isEqualTo(NumberValue(7.0))
    assertThat(evaluate("=(1+2)*3")).isEqualTo(NumberValue(9.0))
    assertThat(evaluate("=-2^2")).isEqualTo(NumberValue(4.0))
    assertThat(evaluate("=2^3^2")).isEqualTo(NumberValue(64.0))
    assertThat(evaluate("=10-2-3")).isEqualTo(NumberValue(5.0))
    assertThat(evaluate("=-10%")).isEqualTo(NumberValue(-0.1))
    assertThat(evaluate("=10%^2")).isEqualTo(NumberValue(0.01))
    assertThat(evaluate("=1+1&1")).isEqualTo(TextValue("21"))
    assertThat(evaluate("=1&1=\"11\"")).isEqualTo(BooleanValue(true))
  }

  @Test
  fun `Whitespace is ignored`() = runTest {
    assertThat(evaluate("= 1 +  2 ")).isEqualTo(NumberValue(3.0))
    assertThat(evaluate("=TRUE( )")).isEqualTo(BooleanValue(true))
  }

  @Test
  fun `Parse errors`() = runTest {
    assertThat(evaluate("=")).isError(Generic)
    assertThat(evaluate("=   ")).isError(Generic)
    assertThat(evaluate("=1+")).isError(Generic)
    assertThat(evaluate("=(1")).isError(Generic)
    assertThat(evaluate("=1+2 )")).isError(Generic)
    assertThat(evaluate("=(1)(2)")).isError(Generic)
    assertThat(evaluate("=\"abc")).isError(Generic)
    assertThat(evaluate("=1E5")).isError(Generic)
    assertThat(evaluate("=5%%")).isError(Generic)
    assertThat(evaluate("=#FOO!")).isError(Generic)
    assertThat(evaluate("=r1c1")).isError(Generic)
  }

  @Test
  fun `Cell ranges aren't supported`() = runTest {
    assertThat(evaluate("=A1:B2")).isError(Generic, "Parsing error. Cell ranges aren't supported")
  }

  @Test
  fun `Arithmetic`() = runTest {
    assertThat(evaluate("=0.1+0.2")).isEqualTo(NumberValue(0.3))
    assertThat(evaluate("=1-0.9")).isEqualTo(NumberValue(0.1))
    assertThat(evaluate("=1/3*3")).isEqualTo(NumberValue(1.0))
    assertThat(evaluate("=1-1e-15")).isEqualTo(NumberValue(1.0))
    assertThat(evaluate("=2^-1")).isEqualTo(NumberValue(0.5))
    assertThat(evaluate("=-\"5\"")).isEqualTo(NumberValue(-5.0))
    assertThat(evaluate("=\"5\"%")).isEqualTo(NumberValue(0.05))
    assertThat(evaluate("=+\"a\"")).isEqualTo(TextValue("a"))
  }

  @Test
  fun `Numbers are rounded to 10 significant digits`() = runTest {
    assertThat(evaluate("=1/3")).isEqualTo(NumberValue(0.33333333333))
    assertThat(evaluate("=1/7")).isEqualTo(NumberValue(0.14285714286))
    assertThat(evaluate("=-1/7")).isEqualTo(NumberValue(-0.14285714286))
    assertThat(evaluate("=123456789.123456")).isEqualTo(NumberValue(123456789.12))
    assertThat(evaluate("=100000000000*3.3")).isEqualTo(NumberValue(330000000000.0))
  }

  @Test
  fun `Arithmetic errors`() = runTest {
    assertThat(evaluate("=1/0")).isError(DivByZero)
    assertThat(evaluate("=0^-1")).isError(Num)
    assertThat(evaluate("=(-8)^(1/3)")).isError(Num)
    assertThat(evaluate("=1e308*10")).isError(Num)
    assertThat(evaluate("=1e400")).isError(Num)
    assertThat(evaluate("=\"abc\"+1")).isError(FormulaErrorType.Value, NUMBER_COERCION_MESSAGE)
  }

  @Test
  fun `Errors propagate from the left first`() = runTest {
    assertThat(evaluate("=1+1/0")).isError(DivByZero)
    assertThat(evaluate("=1/0+FOO()")).isError(DivByZero)
    assertThat(evaluate("=FOO()+1/0")).isError(Name)
    assertThat(evaluate("=\"x\"&#N/A")).isError(NotAvailable)
    assertThat(evaluate("=-#DIV/0!")).isError(DivByZero)
  }

  @Test
  fun `Concatenation converts like JavaScript`() = runTest {
    assertThat(evaluate("=1&2")).isEqualTo(TextValue("12"))
    assertThat(evaluate("=FALSE()&TRUE()")).isEqualTo(TextValue("FALSETRUE"))
    assertThat(evaluate("=1/3&\"\"")).isEqualTo(TextValue("0.3333333333333333"))
    assertThat(evaluate("=1.5e-7&\"\"")).isEqualTo(TextValue("1.5e-7"))
    assertThat(evaluate("=0.000001&\"\"")).isEqualTo(TextValue("0.000001"))
    assertThat(evaluate("=1e21&\"\"")).isEqualTo(TextValue("1e+21"))
    assertThat(evaluate("=12345678901234567&\"\"")).isEqualTo(TextValue("12345678901234568"))
    assertThat(evaluate("=B2&\"x\"")).isEqualTo(TextValue("x"))
  }

  @Test
  fun `Comparisons`() = runTest {
    assertThat(evaluate("=1=1.0000000000001")).isEqualTo(BooleanValue(true))
    assertThat(evaluate("=0.1+0.2=0.3")).isEqualTo(BooleanValue(true))
    assertThat(evaluate("=2<>3")).isEqualTo(BooleanValue(true))
    assertThat(evaluate("=2>=2")).isEqualTo(BooleanValue(true))
    assertThat(evaluate("=2<=1")).isEqualTo(BooleanValue(false))
    assertThat(evaluate("=\"a\"=\"A\"")).isEqualTo(BooleanValue(true))
    assertThat(evaluate("=\"é\"=\"e\"")).isEqualTo(BooleanValue(true))
    assertThat(evaluate("=\"a\"<\"B\"")).isEqualTo(BooleanValue(true))
    assertThat(evaluate("=\"abc\"<\"abd\"")).isEqualTo(BooleanValue(true))
  }

  @Test
  fun `Comparing different types orders numbers, then text, then booleans`() = runTest {
    assertThat(evaluate("=1<\"a\"")).isEqualTo(BooleanValue(true))
    assertThat(evaluate("=\"1\"=1")).isEqualTo(BooleanValue(false))
    assertThat(evaluate("=TRUE()>1")).isEqualTo(BooleanValue(true))
    assertThat(evaluate("=TRUE()=1")).isEqualTo(BooleanValue(false))
    assertThat(evaluate("=\"\"=0")).isEqualTo(BooleanValue(false))
    assertThat(evaluate("=\"\"=FALSE()")).isEqualTo(BooleanValue(false))
  }

  @Test
  fun `Empty values compare as the other side's type`() = runTest {
    assertThat(evaluate("=B2=0")).isEqualTo(BooleanValue(true))
    assertThat(evaluate("=B2=\"\"")).isEqualTo(BooleanValue(true))
    assertThat(evaluate("=B2=FALSE()")).isEqualTo(BooleanValue(true))
  }

  @Test
  fun `Date strings compare as dates`() = runTest {
    assertThat(evaluate("=\"2025-01-31\"<\"2025-02-01\"")).isEqualTo(BooleanValue(true))
    assertThat(evaluate("=\"2025-01-31\"=\"31/01/2025\"")).isEqualTo(BooleanValue(true))
    assertThat(evaluate("=\"2025-01-31\"=45688")).isEqualTo(BooleanValue(true))
  }

  @Test
  fun `Cell references are empty, except the formula's own cell`() = runTest {
    assertThat(evaluate("=B2")).isEqualTo(EmptyValue)
    assertThat(evaluate("=B2+1")).isEqualTo(NumberValue(1.0))
    assertThat(evaluate("=A1")).isError(Cycle)
    assertThat(evaluate("=\$A\$1")).isError(Cycle)
    assertThat(evaluate("=a1+1")).isError(Cycle)
  }

  @Test
  fun `Variables are looked up ignoring case`() = runTest {
    val variables = mapOf("RESULT" to NumberValue(5.0), "theme_x" to TextValue("#ff0000"))
    assertThat(evaluate("=RESULT*2", variables)).isEqualTo(NumberValue(10.0))
    assertThat(evaluate("=result", variables)).isEqualTo(NumberValue(5.0))
    assertThat(evaluate("=THEME_X", variables)).isEqualTo(TextValue("#ff0000"))
    assertThat(evaluate("=bar", variables)).isError(Name, "Named expression bar not recognized.")
    assertThat(evaluate("=ab.c")).isError(Name)
    assertThat(evaluate("=_x")).isError(Name)
  }

  @Test
  fun `Unknown functions are named errors`() = runTest {
    assertThat(evaluate("=FOO(1)")).isError(Name, "Function name FOO not recognized.")
    assertThat(evaluate("=sum(1,2)")).isError(Name, "Function SUM isn't supported yet.")
    assertThat(evaluate("=QUERY(\"a\")")).isError(Name, "Function QUERY isn't supported yet.")
  }

  @Test
  fun `Arrays evaluate to their top left value`() = runTest {
    assertThat(evaluate("={1,2;3,4}")).isEqualTo(NumberValue(1.0))
    assertThat(evaluate("={1;2}")).isEqualTo(NumberValue(1.0))
    assertThat(evaluate("={1,2}+1")).isEqualTo(NumberValue(2.0))
    assertThat(evaluate("={1,\"a\"}&\"x\"")).isEqualTo(TextValue("1x"))
    assertThat(evaluate("={1,2;3}")).isError(Ref, "Array dimensions mismatched.")
    assertThat(evaluate("={}")).isError(Generic)
  }

  @Test
  fun `Parse error messages say what went wrong`() = runTest {
    val value = evaluate("=1+2 )")
    assertThat(value)
      .isInstanceOf<ErrorValue>()
      .prop(ErrorValue::message)
      .startsWith("Parsing error. Unexpected ')'")
  }

  private suspend fun evaluate(
    formula: String,
    variables: Map<String, FormulaValue> = emptyMap(),
  ): FormulaValue = FormulaEvaluator().evaluate(formula, variables).value
}
