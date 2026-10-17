package aktual.budget.formula.domain

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

// Expected values come from running the same formulas through HyperFormula with upstream's config
class DateFunctionsTest {
  @Test
  fun `TODAY is the calendar's date`() = runTest {
    assertThat(evaluate("=TODAY()")).isEqualTo(NumberValue(45322.0))
  }

  @Test
  fun `DATE rolls months and days over`() = runTest {
    assertThat(evaluate("=DATE(2024,1,31)")).isEqualTo(NumberValue(45322.0))
    assertThat(evaluate("=DATE(2024,13,1)")).isEqualTo(NumberValue(45658.0))
    assertThat(evaluate("=DATE(2024,0,1)")).isEqualTo(NumberValue(45261.0))
    assertThat(evaluate("=DATE(2024,1,0)")).isEqualTo(NumberValue(45291.0))
    assertThat(evaluate("=DATE(2024,1.9,1.9)")).isEqualTo(NumberValue(45292.0))
  }

  @Test
  fun `DATE adds 1900 to years before it`() = runTest {
    assertThat(evaluate("=DATE(24,1,1)")).isEqualTo(NumberValue(8767.0))
    assertThat(evaluate("=DATE(1899,12,30)")).isEqualTo(NumberValue(693961.0))
    assertThat(evaluate("=DATE(1500,1,1)")).isEqualTo(NumberValue(547866.0))
    assertThat(evaluate("=DATE(-1,1,1)")).isError(Num, DateOutOfRange)
    assertThat(evaluate("=DATE(10000,1,1)")).isError(Num, DateOutOfRange)
  }

  @Test
  fun `YEAR, MONTH and DAY read a serial number`() = runTest {
    assertThat(evaluate("=YEAR(45322)")).isEqualTo(NumberValue(2024.0))
    assertThat(evaluate("=MONTH(45322.9)")).isEqualTo(NumberValue(1.0))
    assertThat(evaluate("=DAY(45322)")).isEqualTo(NumberValue(31.0))
    assertThat(evaluate("=YEAR(0)")).isEqualTo(NumberValue(1899.0))
    assertThat(evaluate("=DAY(0)")).isEqualTo(NumberValue(30.0))
    assertThat(evaluate("=DAY(60)")).isEqualTo(NumberValue(28.0))
    assertThat(evaluate("""=YEAR("2024-01-31")""")).isEqualTo(NumberValue(2024.0))
    assertThat(evaluate("=YEAR(1e9)")).isEqualTo(NumberValue(2739807.0))
    assertThat(evaluate("=YEAR(-1)")).isError(Num, ValueTooSmall)
  }

  @Test
  fun `EOMONTH and EDATE shift by whole months`() = runTest {
    assertThat(evaluate("=EOMONTH(DATE(2024,1,15),1)")).isEqualTo(NumberValue(45351.0))
    assertThat(evaluate("=EOMONTH(DATE(2024,1,15),-1)")).isEqualTo(NumberValue(45291.0))
    assertThat(evaluate("=EOMONTH(DATE(2024,1,15),1.5)")).isError(Num, DateOutOfRange)
    assertThat(evaluate("=EOMONTH(1e7,0)")).isError(Num, DateOutOfRange)
    assertThat(evaluate("=EDATE(DATE(2024,1,31),0.5)")).isError(Num, DateOutOfRange)
    assertThat(evaluate("=EDATE(0,-1)")).isError(Num, DateOutOfRange)
  }

  @Test
  fun `EDATE ignores leap years when clamping the day`() = runTest {
    assertThat(evaluate("=EDATE(DATE(2024,1,31),1)")).isEqualTo(NumberValue(45350.0))
    assertThat(evaluate("=EDATE(DATE(2024,3,31),-1)")).isEqualTo(NumberValue(45350.0))
  }

  @Test
  fun `DAYS subtracts whole days`() = runTest {
    assertThat(evaluate("=DAYS(DATE(2024,3,1),DATE(2024,2,1))")).isEqualTo(NumberValue(29.0))
    assertThat(evaluate("=DAYS(10.9,1.1)")).isEqualTo(NumberValue(9.0))
    assertThat(evaluate("""=DAYS("2024-03-01","2024-02-01")""")).isEqualTo(NumberValue(29.0))
  }

  @Test
  fun `DAYS360 counts every month as 30 days`() = runTest {
    assertThat(evaluate("=DAYS360(DATE(2024,1,31),DATE(2024,3,31))")).isEqualTo(NumberValue(60.0))
    assertThat(evaluate("=DAYS360(DATE(2024,1,15),DATE(2024,3,31))")).isEqualTo(NumberValue(76.0))
    assertThat(evaluate("=DAYS360(DATE(2024,3,31),DATE(2024,1,31))")).isEqualTo(NumberValue(-60.0))
    assertThat(evaluate("""=DAYS360("2024-01-31","2024-03-31")""")).isEqualTo(NumberValue(60.0))
    assertThat(evaluate("""=DAYS360(1,2,"x")""")).isError(Value, WrongArgType)
  }

  @Test
  fun `DAYS360 only moves the end of February with the US method`() = runTest {
    assertThat(evaluate("=DAYS360(DATE(2024,2,29),DATE(2025,2,28))")).isEqualTo(NumberValue(360.0))
    assertThat(evaluate("=DAYS360(DATE(2023,2,28),DATE(2024,2,29))")).isEqualTo(NumberValue(360.0))
    assertThat(evaluate("=DAYS360(DATE(2024,2,29),DATE(2025,2,28),TRUE())"))
      .isEqualTo(NumberValue(359.0))
  }

  @Test
  fun `YEARFRAC uses the basis to count days`() = runTest {
    val start = "DATE(2024,1,31)"
    val end = "DATE(2024,3,31)"
    assertThat(evaluate("=YEARFRAC($start,$end)")).isEqualTo(NumberValue(0.16666666667))
    assertThat(evaluate("=YEARFRAC($start,$end,1)")).isEqualTo(NumberValue(0.16393442623))
    assertThat(evaluate("=YEARFRAC($start,$end,2)")).isEqualTo(NumberValue(0.16666666667))
    assertThat(evaluate("=YEARFRAC($start,$end,3)")).isEqualTo(NumberValue(0.16438356164))
    assertThat(evaluate("=YEARFRAC($start,$end,4)")).isEqualTo(NumberValue(0.16666666667))
    assertThat(evaluate("=YEARFRAC($end,$start)")).isEqualTo(NumberValue(0.16666666667))
    assertThat(evaluate("=YEARFRAC(10.9,11.1,3)")).isEqualTo(NumberValue(0.0027397260274))
    assertThat(evaluate("""=YEARFRAC("2024-01-01","2025-01-01")""")).isEqualTo(NumberValue(1.0))
  }

  @Test
  fun `YEARFRAC averages year lengths across years with the actual basis`() = runTest {
    assertThat(evaluate("=YEARFRAC(DATE(2023,3,1),DATE(2024,3,1),1)")).isEqualTo(NumberValue(1.0))
    assertThat(evaluate("=YEARFRAC(DATE(2023,6,1),DATE(2024,2,1),1)"))
      .isEqualTo(NumberValue(0.67123287671))
    assertThat(evaluate("=YEARFRAC(DATE(2023,1,1),DATE(2023,6,1),1)"))
      .isEqualTo(NumberValue(0.41369863014))
    assertThat(evaluate("=YEARFRAC(DATE(2024,3,1),DATE(2025,2,1),1)"))
      .isEqualTo(NumberValue(0.92328767123))
    assertThat(evaluate("=YEARFRAC(DATE(2020,1,1),DATE(2026,6,1),1)"))
      .isEqualTo(NumberValue(6.4141572155))
  }

  @Test
  fun `YEARFRAC rejects out of range arguments`() = runTest {
    assertThat(evaluate("=YEARFRAC(1,2,5)")).isError(Num, ValueTooLarge)
    assertThat(evaluate("=YEARFRAC(1,2,-1)")).isError(Num, ValueTooSmall)
    assertThat(evaluate("=YEARFRAC(1,2,1.5)")).isError(Num, IntegerExpected)
    assertThat(evaluate("=YEARFRAC(-1,2)")).isError(Num, ValueTooSmall)
  }
}
