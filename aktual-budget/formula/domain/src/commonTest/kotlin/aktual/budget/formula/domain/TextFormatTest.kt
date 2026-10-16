package aktual.budget.formula.domain

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

// Expected values come from running the same formulas through HyperFormula with upstream's config
class TextFormatTest {
  @Test
  fun `Number patterns round half up`() = runTest {
    assertThat(text("1234.567", "0.00")).isEqualTo("1234.57")
    assertThat(text("-1.005", "0.00")).isEqualTo("-1.00")
    assertThat(text("2.5", "0")).isEqualTo("3")
    assertThat(text("1.25", "0.0")).isEqualTo("1.3")
    assertThat(text("-0.001", "0.00")).isEqualTo("0.00")
  }

  @Test
  fun `Number patterns pad with zeros but not hashes`() = runTest {
    assertThat(text("5", "000")).isEqualTo("005")
    assertThat(text("5", "###")).isEqualTo("5")
    assertThat(text("0.5", "#.##")).isEqualTo("0.5")
    assertThat(text("1.5", "0.0#")).isEqualTo("1.5")
  }

  @Test
  fun `Only the first number pattern is used`() = runTest {
    assertThat(text("1234.567", "#,##0.00")).isEqualTo("1235,##0.00")
    assertThat(text("1234.567", "$0.00")).isEqualTo("$1234.57")
    assertThat(text("0.5", "0%")).isEqualTo("1%")
    assertThat(text("1e21", "0.00")).isEqualTo("1e+21.00")
  }

  @Test
  fun `Formats without patterns are returned as is`() = runTest {
    assertThat(text("1", "abc")).isEqualTo("abc")
    assertThat(text("45322", """\d""")).isEqualTo("""\d""")
    assertThat(evaluate("""=TEXT("abc","0")""")).isError(Value, NumberCoercion)
  }

  @Test
  fun `Date patterns`() = runTest {
    assertThat(text("45322", "yyyy-mm-dd")).isEqualTo("2024-01-31")
    assertThat(text("45322", "dd/mm/yy")).isEqualTo("31/01/24")
    assertThat(text("45322", "d")).isEqualTo("31")
    assertThat(text("45322", "M/D/YYYY")).isEqualTo("1/31/2024")
    assertThat(text("45322.5", "mmm")).isEqualTo("010")
    assertThat(text("45322.5", "yyyy m")).isEqualTo("2024 1")
    assertThat(text("-1", "yyyy-mm-dd")).isEqualTo("1899-12-29")
    assertThat(text("123", "0.00 USD")).isEqualTo("0.00 US2")
  }

  @Test
  fun `Time patterns`() = runTest {
    assertThat(text("45322.75", "hh:mm")).isEqualTo("18:00")
    assertThat(text("45322.75", "h:mm AM/PM")).isEqualTo("6:00 PM")
    assertThat(text("45322.123456", "hh:mm:ss.000")).isEqualTo("02:57:46.598")
    assertThat(text("45322.123456", "ss.00")).isEqualTo("46.59")
    assertThat(text("0.999999999", "hh:mm:ss")).isEqualTo("23:59:59")
    assertThat(text("45322.5", "h m")).isEqualTo("12 0")
    assertThat(text("45322.5", "m:ss")).isEqualTo("0:00")
    assertThat(text("1.5", "a/p h")).isEqualTo("p 12")
  }

  @Test
  fun `Duration patterns`() = runTest {
    assertThat(text("1.5", "[hh]:mm")).isEqualTo("36:00")
    assertThat(text("1.5", "[mm]")).isEqualTo("2160")
  }

  private suspend fun text(value: String, format: String): String {
    val result = evaluate("""=TEXT($value,"$format")""")
    return (result as TextValue).value
  }
}
