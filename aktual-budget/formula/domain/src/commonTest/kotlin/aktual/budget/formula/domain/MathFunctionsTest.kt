package aktual.budget.formula.domain

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

// Expected values come from running the same formulas through HyperFormula with upstream's config
class MathFunctionsTest {
  @Test
  fun `Aggregates coerce plain arguments but skip non-numbers in arrays`() = runTest {
    assertThat(evaluate("""=SUM({"a",1})""")).isEqualTo(NumberValue(1.0))
    assertThat(evaluate("=SUM({1,TRUE()})")).isEqualTo(NumberValue(1.0))
    assertThat(evaluate("""=AVERAGE(1,2,"3")""")).isEqualTo(NumberValue(2.0))
    assertThat(evaluate("=PRODUCT({2,3},4)")).isEqualTo(NumberValue(24.0))
    assertThat(evaluate("=SUM(,)")).isEqualTo(NumberValue(0.0))
    assertThat(evaluate("=SUM(B1)")).isEqualTo(NumberValue(0.0))
    assertThat(evaluate("""=SUM("abc")""")).isError(Value, NumberCoercion)
    assertThat(evaluate("""=SUM({"a"})""")).isError(Value, NumberCoercion)
    assertThat(evaluate("=SUM()")).isError(NotAvailable, WrongArgCount)
  }

  @Test
  fun `Aggregates return the first error`() = runTest {
    assertThat(evaluate("=SUM({1,#N/A})")).isError(NotAvailable)
    assertThat(evaluate("=SUM(1/0,#N/A)")).isError(DivByZero)
    assertThat(evaluate("=SUM(A1)")).isError(Cycle)
  }

  @Test
  fun `COUNT counts numbers after coercion`() = runTest {
    assertThat(evaluate("""=COUNT("a",1,"1",TRUE,)""")).isEqualTo(NumberValue(3.0))
    assertThat(evaluate("""=COUNT({1,"a",TRUE()})""")).isEqualTo(NumberValue(1.0))
    assertThat(evaluate("=COUNT(1/0)")).isEqualTo(NumberValue(0.0))
  }

  @Test
  fun `MIN, MAX and AVERAGE without numbers`() = runTest {
    assertThat(evaluate("=MIN(B1)")).isEqualTo(NumberValue(0.0))
    assertThat(evaluate("""=MAX({1,"a"})""")).isEqualTo(NumberValue(1.0))
    assertThat(evaluate("""=AVERAGE({"a",B1})""")).isError(DivByZero)
    assertThat(evaluate("=MAX()")).isError(NotAvailable, WrongArgCount)
  }

  @Test
  fun `Single number functions`() = runTest {
    assertThat(evaluate("=ABS({-1,-2})")).isEqualTo(NumberValue(1.0))
    assertThat(evaluate("=SUM(ABS({-1,-2}))")).isEqualTo(NumberValue(1.0))
    assertThat(evaluate("=MOD(-1,3)")).isEqualTo(NumberValue(-1.0))
    assertThat(evaluate("=MOD(1,0)")).isError(DivByZero)
    assertThat(evaluate("=SIGN(-0)")).isEqualTo(NumberValue(0.0))
    assertThat(evaluate("=SIGN(-3)")).isEqualTo(NumberValue(-1.0))
    assertThat(evaluate("=SQRT(-1)")).isError(Num, NotFinite)
    assertThat(evaluate("=POWER(2,0.5)")).isEqualTo(NumberValue(1.4142135624))
    assertThat(evaluate("=INT(-1.5)")).isEqualTo(NumberValue(-1.0))
  }

  @Test
  fun `Rounding is symmetric around zero`() = runTest {
    assertThat(evaluate("=ROUND(2.5)")).isEqualTo(NumberValue(3.0))
    assertThat(evaluate("=ROUND(-2.5)")).isEqualTo(NumberValue(-3.0))
    assertThat(evaluate("=ROUND(1.005,2)")).isEqualTo(NumberValue(1.0))
    assertThat(evaluate("=ROUNDUP(1.21,1)")).isEqualTo(NumberValue(1.3))
    assertThat(evaluate("=TRUNC(-1.57,1)")).isEqualTo(NumberValue(-1.5))
  }

  @Test
  fun `CEILING and FLOOR round to a multiple`() = runTest {
    assertThat(evaluate("=CEILING(-2.5,2)")).isEqualTo(NumberValue(-2.0))
    assertThat(evaluate("=CEILING(2.5,-2)")).isError(Num, DistinctSigns)
    assertThat(evaluate("=FLOOR(-2.5,-2)")).isEqualTo(NumberValue(-2.0))
    assertThat(evaluate("=CEILING(0,0)")).isEqualTo(NumberValue(0.0))
    assertThat(evaluate("=CEILING(1,0)")).isError(DivByZero)
  }
}
