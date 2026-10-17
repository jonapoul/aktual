package aktual.budget.formula

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

  @Test
  fun `COUNTA counts every value that isn't empty`() = runTest {
    assertThat(evaluate("""=COUNTA(1,"x",TRUE(),{1,"a",""})""")).isEqualTo(NumberValue(6.0))
    assertThat(evaluate("=COUNTA(1,,2)")).isEqualTo(NumberValue(3.0))
    assertThat(evaluate("=COUNTA(#N/A)")).isEqualTo(NumberValue(1.0))
    assertThat(evaluate("=COUNTA({1,#N/A})")).isEqualTo(NumberValue(2.0))
  }

  @Test
  fun `COUNTBLANK counts empty arguments`() = runTest {
    assertThat(evaluate("""=COUNTBLANK(1,"",{1,""})""")).isEqualTo(NumberValue(0.0))
    assertThat(evaluate("=COUNTBLANK(1,,2)")).isEqualTo(NumberValue(1.0))
  }

  @Test
  fun `VAR, VARP, STDEV and STDEVP measure spread`() = runTest {
    assertThat(evaluate("=VAR(1,2,3,4)")).isEqualTo(NumberValue(1.6666666667))
    assertThat(evaluate("=VARP(1,2,3,4)")).isEqualTo(NumberValue(1.25))
    assertThat(evaluate("=STDEV(1,2,3,4)")).isEqualTo(NumberValue(1.2909944487))
    assertThat(evaluate("=STDEVP(1,2,3,4)")).isEqualTo(NumberValue(1.1180339887))
    assertThat(evaluate("=STDEVP(0.1,0.2,0.3)")).isEqualTo(NumberValue(0.081649658093))
    assertThat(evaluate("""=STDEV({1,2,"x",TRUE()},"3")""")).isEqualTo(NumberValue(1.0))
    assertThat(evaluate("=VAR(1)")).isError(DivByZero)
    assertThat(evaluate("""=VAR("x")""")).isError(Value, NumberCoercion)
  }

  @Test
  fun `MEDIAN is the middle number`() = runTest {
    assertThat(evaluate("=MEDIAN(3,1,2)")).isEqualTo(NumberValue(2.0))
    assertThat(evaluate("=MEDIAN(4,1,3,2)")).isEqualTo(NumberValue(2.5))
    assertThat(evaluate("""=MEDIAN({5,"x",TRUE()},1)""")).isEqualTo(NumberValue(3.0))
    assertThat(evaluate("""=MEDIAN("3",1)""")).isEqualTo(NumberValue(2.0))
    assertThat(evaluate("=MEDIAN(TRUE(),3)")).isEqualTo(NumberValue(2.0))
    assertThat(evaluate("""=MEDIAN("x",1)""")).isError(Value, NumberCoercion)
    assertThat(evaluate("""=MEDIAN({"x"})""")).isError(Value, NumberCoercion)
    assertThat(evaluate("""=MEDIAN({1,#N/A},"x")""")).isError(Value, NumberCoercion)
  }

  @Test
  fun `EXP, LN, LOG and LOG10 are exponents and logarithms`() = runTest {
    assertThat(evaluate("=EXP(1)")).isEqualTo(NumberValue(2.7182818285))
    assertThat(evaluate("=LN(10)")).isEqualTo(NumberValue(2.302585093))
    assertThat(evaluate("=LOG(100)")).isEqualTo(NumberValue(2.0))
    assertThat(evaluate("=LOG(8,2)")).isEqualTo(NumberValue(3.0))
    assertThat(evaluate("=LOG10(1000)")).isEqualTo(NumberValue(3.0))
    assertThat(evaluate("=LN(0)")).isError(Num, NotFinite)
    assertThat(evaluate("=LN(-1)")).isError(Num, NotFinite)
    assertThat(evaluate("=LOG(8,1)")).isError(Num, NotFinite)
    assertThat(evaluate("=LOG(0)")).isError(Num, ValueTooSmall)
    assertThat(evaluate("=LOG10(0)")).isError(Num, NotFinite)
  }

  @Test
  fun `PI is rounded like upstream's`() = runTest {
    assertThat(evaluate("=PI()")).isEqualTo(NumberValue(3.1415926536))
  }
}
