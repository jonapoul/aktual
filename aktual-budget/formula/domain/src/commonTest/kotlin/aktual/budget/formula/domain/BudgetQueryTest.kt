package aktual.budget.formula.domain

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.YearMonth

class BudgetQueryTest {
  @Test
  fun `Budgeted, spent and goal are summed over the months`() = runTest {
    assertThat(evaluate("""=BUDGET_QUERY("budgeted", CATEGORIES, "2024-01", "2024-02")"""))
      .isEqualTo(NumberValue(130.0))
    assertThat(evaluate("""=BUDGET_QUERY("spent", CATEGORIES, "2024-01", "2024-02")"""))
      .isEqualTo(NumberValue(-86.0))
    assertThat(evaluate("""=BUDGET_QUERY("goal", CATEGORIES, "2024-01", "2024-02")"""))
      .isEqualTo(NumberValue(80.0))
  }

  @Test
  fun `Balances chain through the months`() = runTest {
    assertThat(evaluate("""=BUDGET_QUERY("balance_start", CATEGORIES, "2024-01", "2024-02")"""))
      .isEqualTo(NumberValue(50.0))
    assertThat(evaluate("""=BUDGET_QUERY("balance_end", CATEGORIES, "2024-01", "2024-01")"""))
      .isEqualTo(NumberValue(90.0))
    assertThat(evaluate("""=BUDGET_QUERY("balance_end", CATEGORIES, "2024-01", "2024-02")"""))
      .isEqualTo(NumberValue(94.0))
  }

  @Test
  fun `Dimensions ignore case`() = runTest {
    assertThat(evaluate("""=BUDGET_QUERY("Budgeted", "cat-1", "2024-01", "2024-01")"""))
      .isEqualTo(NumberValue(100.0))
  }

  @Test
  fun `Categories can be a comma separated text`() = runTest {
    assertThat(evaluate("""=BUDGET_QUERY("budgeted", "cat-1, cat-2,", "2024-01", "2024-01")"""))
      .isEqualTo(NumberValue(120.0))
  }

  @Test
  fun `Unknown categories and months are zero`() = runTest {
    assertThat(evaluate("""=BUDGET_QUERY("budgeted", "nope", "2024-01", "2024-02")"""))
      .isEqualTo(NumberValue(0.0))
    assertThat(evaluate("""=BUDGET_QUERY("budgeted", CATEGORIES, "2030-01", "2030-02")"""))
      .isEqualTo(NumberValue(0.0))
  }

  @Test
  fun `An end before the start is zero`() = runTest {
    assertThat(evaluate("""=BUDGET_QUERY("budgeted", CATEGORIES, "2024-02", "2024-01")"""))
      .isEqualTo(NumberValue(0.0))
    assertThat(evaluate("""=BUDGET_QUERY("balance_end", CATEGORIES, "2024-02", "2024-01")"""))
      .isEqualTo(NumberValue(0.0))
  }

  @Test
  fun `Invalid dimensions are errors`() = runTest {
    assertThat(evaluate("""=BUDGET_QUERY("nope", CATEGORIES, "2024-01", "2024-02")"""))
      .isEqualTo(ErrorValue(Value, InvalidBudgetDimension, "nope"))
  }

  @Test
  fun `Invalid months are errors`() = runTest {
    assertThat(evaluate("""=BUDGET_QUERY("spent", CATEGORIES, "January", "2024-02")"""))
      .isEqualTo(ErrorValue(Value, InvalidDate))
    assertThat(evaluate("""=BUDGET_QUERY("spent", CATEGORIES, "2024-01", "2024-13")"""))
      .isEqualTo(ErrorValue(Value, InvalidDate))
  }

  @Test
  fun `Arguments are required`() = runTest {
    assertThat(evaluate("""=BUDGET_QUERY("spent", CATEGORIES, "2024-01")"""))
      .isEqualTo(ErrorValue(NotAvailable, WrongArgCount))
  }

  private suspend fun evaluate(formula: String): FormulaValue =
    evaluateWithWarnings(
        formula,
        variables =
          mapOf(
            "CATEGORIES" to
              ArrayValue(listOf(listOf(TextValue("cat-1")), listOf(TextValue("cat-2")))),
          ),
        data = DATA,
      )
      .value

  private companion object {
    val DATA =
      FakeFormulaDataSource(
        budgets =
          mapOf(
            YearMonth(2023, 12) to
              mapOf(
                "cat-1" to budget(balance = 5000),
                "cat-2" to budget(balance = -2000),
              ),
            YearMonth(2024, 1) to
              mapOf(
                "cat-1" to budget(budgeted = 10000, spent = -3000, goal = 8000, balance = 12000),
                "cat-2" to
                  budget(budgeted = 2000, spent = -5000, balance = -3000, carryover = true),
              ),
            YearMonth(2024, 2) to
              mapOf(
                "cat-1" to budget(budgeted = 1000, spent = -500),
                "cat-2" to budget(spent = -100),
              ),
          ),
      )

    fun budget(
      budgeted: Long = 0,
      spent: Long = 0,
      goal: Long = 0,
      balance: Long = 0,
      carryover: Boolean = false,
    ) = CategoryBudget(budgeted, spent, goal, balance, carryover)
  }
}
