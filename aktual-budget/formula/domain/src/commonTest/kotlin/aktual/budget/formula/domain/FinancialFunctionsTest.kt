package aktual.budget.formula.domain

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

// Expected values come from running the same formulas through HyperFormula with upstream's config
class FinancialFunctionsTest {
  @Test
  fun `PMT is the payment per period`() = runTest {
    assertThat(evaluate("=PMT(0.05/12,360,300000)")).isEqualTo(NumberValue(-1610.464869))
    assertThat(evaluate("=PMT(0.05/12,360,300000,0,1)")).isEqualTo(NumberValue(-1603.7824422))
    assertThat(evaluate("=PMT(0.05/12,360,300000,10000,2)")).isEqualTo(NumberValue(-1615.748081))
    assertThat(evaluate("=PMT(0,12,1200)")).isEqualTo(NumberValue(-100.0))
  }

  @Test
  fun `IPMT and PPMT split a payment into interest and principal`() = runTest {
    assertThat(evaluate("=IPMT(0.05/12,1,360,-300000)")).isEqualTo(NumberValue(1250.0))
    assertThat(evaluate("=IPMT(0.05/12,2,360,-300000)")).isEqualTo(NumberValue(1248.498063))
    assertThat(evaluate("=IPMT(0.05/12,1,360,-300000,0,1)")).isEqualTo(NumberValue(0.0))
    assertThat(evaluate("=IPMT(0.05/12,3,360,-300000,0,1)")).isEqualTo(NumberValue(1241.8156362))
    assertThat(evaluate("=PPMT(0.05/12,2,360,-300000)")).isEqualTo(NumberValue(361.96680599))
    assertThat(evaluate("=PPMT(0.05/12,3,360,-300000,0,1)")).isEqualTo(NumberValue(361.96680599))
  }

  @Test
  fun `FV is the value after the payments`() = runTest {
    assertThat(evaluate("=FV(0.04/12,120,-200)")).isEqualTo(NumberValue(29449.960945))
    assertThat(evaluate("=FV(0.04/12,120,-200,-1000,1)")).isEqualTo(NumberValue(31038.960164))
    assertThat(evaluate("=FV(0,10,-5,-100)")).isEqualTo(NumberValue(150.0))
  }

  @Test
  fun `CUMIPMT and CUMPRINC add up a range of periods`() = runTest {
    assertThat(evaluate("=CUMIPMT(0.05/12,360,300000,1,12,0)"))
      .isEqualTo(NumberValue(-14899.482392))
    assertThat(evaluate("=CUMPRINC(0.05/12,360,300000,1,12,0)"))
      .isEqualTo(NumberValue(-4426.0960367))
    assertThat(evaluate("=CUMIPMT(0.05/12,360,300000,13,24,1)"))
      .isEqualTo(NumberValue(-14612.150956))
  }

  @Test
  fun `CUMIPMT rejects out of range arguments`() = runTest {
    assertThat(evaluate("=CUMIPMT(0.05/12,360,300000,12,1,0)")).isError(Num, StartAfterEnd)
    assertThat(evaluate("=CUMIPMT(0.05/12,360,-300000,1,12,0)")).isError(Num, ValueTooSmall)
    assertThat(evaluate("=CUMIPMT(0.05/12,360,300000,1,12,2)")).isError(Num, ValueTooLarge)
    assertThat(evaluate("=CUMIPMT(0.05/12,360,300000,1.5,12,0)")).isError(Num, IntegerExpected)
  }

  @Test
  fun `NPER is the number of payments`() = runTest {
    assertThat(evaluate("=NPER(0.05/12,-1000,50000)")).isEqualTo(NumberValue(56.184290761))
    assertThat(evaluate("=NPER(0.05/12,-1000,50000,0,1)")).isEqualTo(NumberValue(55.92182249))
    assertThat(evaluate("=NPER(0,-100,1000)")).isEqualTo(NumberValue(10.0))
    assertThat(evaluate("=NPER(0,0,1000)")).isError(DivByZero)
  }

  @Test
  fun `RATE finds the rate per period`() = runTest {
    assertThat(evaluate("=RATE(360,-1610.46,300000)")).isEqualTo(NumberValue(0.0041666445363))
    assertThat(evaluate("=RATE(12,-100,1000,0,1)")).isEqualTo(NumberValue(0.035031530362))
    assertThat(evaluate("=RATE(10,100,1000)")).isError(Num)
    assertThat(evaluate("=RATE(12,-100,1000,0,0,-1)")).isError(Value)
    assertThat(evaluate("=RATE(0,-100,1000)")).isError(Num, ValueTooSmall)
  }

  @Test
  fun `PV is the present value of the payments`() = runTest {
    assertThat(evaluate("=PV(0.05/12,360,-1610.46)")).isEqualTo(NumberValue(299999.09299))
    assertThat(evaluate("=PV(0.05/12,360,-1610.46,0,1)")).isEqualTo(NumberValue(301249.08921))
    assertThat(evaluate("=PV(0,12,-100,50)")).isEqualTo(NumberValue(1150.0))
    assertThat(evaluate("=PV(-1,0,1)")).isError(Num)
    assertThat(evaluate("=PV(-1,2,1)")).isError(DivByZero)
  }

  @Test
  fun `NPV discounts each value by another period`() = runTest {
    assertThat(evaluate("=NPV(0.1,-1000,300,400,500)")).isEqualTo(NumberValue(-19.12437675))
    assertThat(evaluate("""=NPV(0.1,{-1000,300},{400,"x",TRUE()},500)"""))
      .isEqualTo(NumberValue(-19.12437675))
    assertThat(evaluate("""=NPV(0.1,"5")""")).isEqualTo(NumberValue(4.5454545455))
    assertThat(evaluate("=NPV(-1,0,0)")).isEqualTo(NumberValue(0.0))
    assertThat(evaluate("=NPV(-1,1)")).isError(DivByZero)
    assertThat(evaluate("""=NPV(0.1,"x")""")).isError(Value, NumberCoercion)
    assertThat(evaluate("""=NPV(0.1,{1,#N/A},"x")""")).isError(Value, NumberCoercion)
  }

  @Test
  fun `XNPV discounts each value by its date`() = runTest {
    assertThat(evaluate("=XNPV(0.1,{-1000,300,800},{45292,45400,45658})"))
      .isEqualTo(NumberValue(18.740617367))
    assertThat(evaluate("=XNPV(0.1,{-1000,300},{45292.7,45400.2})"))
      .isEqualTo(NumberValue(-708.34222649))
    assertThat(evaluate("=XNPV(0.1,-1000,45292)")).isEqualTo(NumberValue(-1000.0))
    assertThat(evaluate("=XNPV(0.1,{-1000,300},{45292})")).isError(Num, ArraySizeMismatch)
    assertThat(evaluate("=XNPV(0.1,{-1000,300},{45292,45000})")).isError(Num, ValueTooSmall)
    assertThat(evaluate("""=XNPV(0.1,{-1000,"x"},{45292,45400})""")).isError(Value, NumberCoercion)
    assertThat(evaluate("=XNPV(-1,1,1)")).isError(Num, ValueTooSmall)
  }

  // Upstream rejects these, since HyperFormula's DATE gives a date typed number that XNPV doesn't
  // count as a number
  @Test
  fun `XNPV accepts dates from DATE`() = runTest {
    assertThat(
        evaluate("=XNPV(0.1,{-1000,300,800},{DATE(2024,1,1),DATE(2024,4,18),DATE(2025,1,1)})"),
      )
      .isEqualTo(NumberValue(18.740617367))
  }
}
