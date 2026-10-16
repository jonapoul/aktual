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
}
