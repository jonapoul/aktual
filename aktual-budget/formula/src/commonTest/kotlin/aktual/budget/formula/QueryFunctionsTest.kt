package aktual.budget.formula

import assertk.all
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.prop
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.YearMonth

class QueryFunctionsTest {
  @Test
  fun `Query sums are amounts`() = runTest {
    assertThat(evaluateWithWarnings("""=QUERY("food")""", data = DATA)).all {
      prop(FormulaResult::value).isEqualTo(NumberValue(-123.45))
      prop(FormulaResult::warnings).isEmpty()
    }
  }

  @Test
  fun `Query counts`() = runTest {
    assertThat(evaluate("""=QUERY_COUNT("food")""")).isEqualTo(NumberValue(7.0))
  }

  @Test
  fun `Unknown queries are zero, with one warning per name`() = runTest {
    val formula = """=QUERY("nope")+QUERY_COUNT("nope")+QUERY("other")"""
    assertThat(evaluateWithWarnings(formula, data = DATA)).all {
      prop(FormulaResult::value).isEqualTo(NumberValue(0.0))
      prop(FormulaResult::warnings).containsExactly(UnknownQuery("nope"), UnknownQuery("other"))
    }
  }

  @Test
  fun `Query names are coerced to text`() = runTest {
    assertThat(evaluate("=QUERY(12)")).isEqualTo(NumberValue(10.0))
    assertThat(evaluate("=QUERY(1/0)")).isError(DivByZero)
    assertThat(evaluate("=QUERY()")).isError(NotAvailable, WrongArgCount)
  }

  @Test
  fun `Extracted categories are a column`() = runTest {
    assertThat(evaluate("""=QUERY_EXTRACT_CATEGORIES("food")""")).isEqualTo(TextValue("cat-1"))
    assertThat(evaluate("""=CONCATENATE(QUERY_EXTRACT_CATEGORIES("food"))"""))
      .isEqualTo(TextValue("cat-1cat-2"))
  }

  @Test
  fun `No extracted categories is empty text`() = runTest {
    assertThat(evaluate("""=QUERY_EXTRACT_CATEGORIES("none")""")).isEqualTo(TextValue(""))
    assertThat(evaluateWithWarnings("""=QUERY_EXTRACT_CATEGORIES("nope")""", data = DATA)).all {
      prop(FormulaResult::value).isEqualTo(TextValue(""))
      prop(FormulaResult::warnings).containsExactly(UnknownQuery("nope"))
    }
  }

  @Test
  fun `Time frames are year-months`() = runTest {
    assertThat(evaluate("""=QUERY_EXTRACT_TIMEFRAME_START("food")"""))
      .isEqualTo(TextValue("2023-11"))
    assertThat(evaluate("""=QUERY_EXTRACT_TIMEFRAME_END("food")""")).isEqualTo(TextValue("2024-02"))
  }

  @Test
  fun `Unknown query time frames are the current month`() = runTest {
    assertThat(evaluateWithWarnings("""=QUERY_EXTRACT_TIMEFRAME_START("nope")""", data = DATA))
      .all {
        prop(FormulaResult::value).isEqualTo(TextValue("2024-01"))
        prop(FormulaResult::warnings).containsExactly(UnknownQuery("nope"))
      }
    assertThat(evaluate("""=QUERY_EXTRACT_TIMEFRAME_END("nope")""")).isEqualTo(TextValue("2024-01"))
  }

  @Test
  fun `Account balances are amounts`() = runTest {
    assertThat(evaluate("""=BALANCE_OF("Checking")""")).isEqualTo(NumberValue(1000.5))
  }

  @Test
  fun `Unknown accounts are zero, with a warning`() = runTest {
    assertThat(evaluateWithWarnings("""=BALANCE_OF("nope")""", data = DATA)).all {
      prop(FormulaResult::value).isEqualTo(NumberValue(0.0))
      prop(FormulaResult::warnings).containsExactly(UnknownAccount("nope"))
    }
  }

  private suspend fun evaluate(formula: String): FormulaValue =
    evaluateWithWarnings(formula, data = DATA).value

  private companion object {
    val DATA =
      FakeFormulaDataSource(
        queries =
          mapOf(
            "food" to
              FakeQuery(
                sum = -12345,
                count = 7,
                categories = listOf("cat-1", "cat-2"),
                months = YearMonth(2023, 11)..YearMonth(2024, 2),
              ),
            "none" to FakeQuery(),
            "12" to FakeQuery(sum = 1000),
          ),
        balances = mapOf("Checking" to 100050),
      )
  }
}
