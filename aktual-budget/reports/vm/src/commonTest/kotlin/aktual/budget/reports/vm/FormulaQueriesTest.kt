package aktual.budget.reports.vm

import aktual.budget.BudgetSyncController
import aktual.budget.budgeting.domain.BudgetMonthCalculatorImpl
import aktual.budget.db.BudgetDatabase
import aktual.budget.db.dao.BudgetDao
import aktual.budget.db.dao.DatabaseTables.ACCOUNTS
import aktual.budget.db.dao.DatabaseTables.CATEGORIES
import aktual.budget.db.dao.DatabaseTables.CATEGORY_GROUPS
import aktual.budget.db.dao.DatabaseTables.CATEGORY_MAPPING
import aktual.budget.db.dao.DatabaseTables.ZERO_BUDGETS
import aktual.budget.db.dao.FormulaDao
import aktual.budget.db.dao.PreferencesDao
import aktual.budget.formula.domain.CategoryBudget
import aktual.budget.formula.domain.FormulaEvaluator
import aktual.budget.formula.domain.NumberValue
import aktual.budget.formula.domain.UnknownQuery
import aktual.budget.model.Condition
import aktual.budget.model.ConditionOp
import aktual.budget.model.Field
import aktual.budget.model.Operator
import alakazam.test.TestCoroutineContexts
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import kotlin.test.Test
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive

class FormulaQueriesTest {
  private var scheduler = TestCoroutineScheduler()

  @Test
  fun `Sums and counts every transaction without a filter or time frame`() = runQueriesTest {
    val queries = queries("all" to query())
    assertThat(queries.querySum("all")).isEqualTo(16350L)
    // Includes the zero amount, but not the split parent or the deleted one
    assertThat(queries.queryCount("all")).isEqualTo(7L)
  }

  @Test
  fun `Each query is read once`() = runSyncedDatabaseTest { _, sync ->
    sync.insertBudget()
    val queries = queries("all" to query())
    assertThat(queries.querySum("all")).isEqualTo(16350L)
    sync.transaction("later", -350)
    assertThat(queries.querySum("all")).isEqualTo(16350L)
    assertThat(queries.queryCount("all")).isEqualTo(7L)
    assertThat(queries("all" to query()).queryCount("all")).isEqualTo(8L)
  }

  @Test
  fun `Time frames limit the dates`() = runQueriesTest {
    val queries = queries("may" to query(timeFrame = may()))
    assertThat(queries.querySum("may")).isEqualTo(-3600L)
    assertThat(queries.queryCount("may")).isEqualTo(5L)
  }

  @Test
  fun `Conditions filter the transactions`() = runQueriesTest {
    val queries = queries("food" to query(condition(Category, Is, text("food"))))
    assertThat(queries.querySum("food")).isEqualTo(-2550L)
    assertThat(queries.queryCount("food")).isEqualTo(4L)
  }

  @Test
  fun `Unknown queries are null`() = runQueriesTest {
    val queries = queries()
    assertThat(queries.querySum("nope")).isNull()
    assertThat(queries.queryCount("nope")).isNull()
    assertThat(queries.queryCategories("nope")).isNull()
    assertThat(queries.queryMonths("nope")).isNull()
  }

  @Test
  fun `Without category conditions, categories are the visible expense ones`() = runQueriesTest {
    val queries =
      queries(
        "none" to query(),
        "amount" to query(condition(Amount, GreaterThan, JsonPrimitive(0))),
      )
    assertThat(queries.queryCategories("none")).isEqualTo(listOf("food", "fuel", "rent"))
    assertThat(queries.queryCategories("amount")).isEqualTo(listOf("food", "fuel", "rent"))
  }

  @Test
  fun `Or takes every category any condition matches`() = runQueriesTest {
    val queries =
      queries(
        "or" to
          query(
            condition(CategoryGroup, Is, text("bills")),
            condition(Category, OneOf, texts("salary", "fuel")),
            op = Or,
          ),
      )
    assertThat(queries.queryCategories("or")).isEqualTo(listOf("rent", "fuel", "salary"))
  }

  @Test
  fun `And takes the categories every condition matches`() = runQueriesTest {
    val queries =
      queries(
        "and" to
          query(
            condition(CategoryGroup, Contains, text("SPEND")),
            condition(Category, Matches, text("^f")),
          ),
        "none" to
          query(
            condition(Category, Is, text("food")),
            condition(Category, IsNot, text("food")),
          ),
      )
    assertThat(queries.queryCategories("and")).isEqualTo(listOf("food", "fuel"))
    assertThat(queries.queryCategories("none")).isEqualTo(emptyList())
  }

  @Test
  fun `Operators other than text and id ones match no categories`() = runQueriesTest {
    val queries = queries("tags" to query(condition(Category, HasTags, text("#x"))))
    assertThat(queries.queryCategories("tags")).isEqualTo(emptyList())
  }

  @Test
  fun `Months come from the time frame, or are the current month`() = runQueriesTest {
    val queries =
      queries(
        "may" to query(timeFrame = may()),
        "window" to
          query(timeFrame = TimeFrame(YearMonth(2020, 1), YearMonth(2020, 3), SlidingWindow)),
        "none" to query(),
      )
    assertThat(queries.queryMonths("may")).isEqualTo(YearMonth(2026, 5)..YearMonth(2026, 5))
    assertThat(queries.queryMonths("window")).isEqualTo(YearMonth(2026, 4)..YearMonth(2026, 6))
    assertThat(queries.queryMonths("none")).isEqualTo(YearMonth(2026, 6)..YearMonth(2026, 6))
  }

  @Test
  fun `Account balances are found by id, then name`() = runQueriesTest {
    val queries = queries()
    assertThat(queries.accountBalance("checking")).isEqualTo(-3650L)
    assertThat(queries.accountBalance("Savings")).isEqualTo(20000L)
    assertThat(queries.accountBalance("Gone")).isNull()
    assertThat(queries.accountBalance("nope")).isNull()
  }

  @Test
  fun `Budget months have the envelope values of each category`() = runQueriesTest {
    val queries = queries()
    val april = queries.budgetMonth(YearMonth(2026, 4))
    assertThat(april["food"])
      .isEqualTo(
        CategoryBudget(
          budgeted = 4000,
          spent = -50,
          goal = 3000,
          balance = 3950,
          carryover = false,
        ),
      )
    val may = queries.budgetMonth(YearMonth(2026, 5))
    assertThat(may["food"])
      .isEqualTo(
        CategoryBudget(budgeted = 500, spent = -2500, goal = 0, balance = 1950, carryover = true),
      )
    assertThat(may["rent"]).isEqualTo(CategoryBudget(0, 0, 0, 0, false))
  }

  @Test
  fun `Formulas read the queries`() = runQueriesTest {
    val evaluator = FormulaEvaluator({ TODAY }, queries("all" to query()))
    val result = evaluator.evaluate("""=QUERY("all")+BALANCE_OF("Savings")+QUERY("nope")""")
    assertThat(result.value).isEqualTo(NumberValue(363.5))
    assertThat(result.warnings).containsExactly(UnknownQuery("nope"))
  }

  private fun BudgetDatabase.queries(vararg queries: Pair<String, FormulaQuery>): FormulaQueries {
    val contexts = TestCoroutineContexts(StandardTestDispatcher(scheduler))
    val budgets =
      BudgetMonthCalculatorImpl(
        budgetDao = BudgetDao(this, contexts),
        preferencesDao = PreferencesDao(this, contexts),
        calendar = { TODAY },
        contexts = contexts,
      )
    return FormulaQueries(queries.toMap(), FormulaDao(this), budgets, calendar = { TODAY })
  }

  private fun query(
    vararg conditions: Condition,
    op: ConditionOp? = null,
    timeFrame: TimeFrame? = null,
  ) = FormulaQuery(conditions.toList(), op, timeFrame)

  private fun condition(field: Field, operator: Operator, value: JsonElement) =
    Condition(field = field, operator = operator, value = value)

  private fun text(value: String) = JsonPrimitive(value)

  private fun texts(vararg values: String) = JsonArray(values.map(::JsonPrimitive))

  private fun may() = TimeFrame(YearMonth(2026, 5), YearMonth(2026, 5), Static)

  private suspend fun BudgetSyncController.insertBudget() {
    row(ACCOUNTS, "checking", "name" to "Checking", "sort_order" to 1L)
    row(ACCOUNTS, "savings", "name" to "Savings", "sort_order" to 2L)
    row(ACCOUNTS, "gone", "name" to "Gone", "tombstone" to true)

    row(CATEGORY_GROUPS, "spending", "name" to "Spending", "sort_order" to 1L)
    row(CATEGORY_GROUPS, "bills", "name" to "Bills", "sort_order" to 2L)
    row(CATEGORY_GROUPS, "income", "name" to "Income", "sort_order" to 3L, "is_income" to true)
    category("food", "Food", "spending", sortOrder = 1L)
    category("fuel", "Fuel", "spending", sortOrder = 2L)
    category("secret", "Secret", "spending", sortOrder = 3L, hidden = true)
    category("rent", "Rent", "bills", sortOrder = 1L)
    category("salary", "Salary", "income", sortOrder = 1L, isIncome = true)

    transaction("plain", -1000, category = "food")
    transaction("split", -3500, isParent = true)
    transaction("split-food", -1500, category = "food", parent = "split")
    transaction("split-fuel", -2000, category = "fuel", parent = "split")
    transaction("zero", 0, category = "food")
    transaction("deleted", -5000, category = "food", isDeleted = true)
    transaction("salary", 900, category = "salary")
    transaction("april", -50, category = "food", date = 20260410)
    transaction("savings", 20000, account = "savings", date = 20260410)

    row(
      ZERO_BUDGETS,
      "202604-food",
      "month" to 202604L,
      "category" to "food",
      "amount" to 4000L,
      "goal" to 3000L,
    )
    row(
      ZERO_BUDGETS,
      "202605-food",
      "month" to 202605L,
      "category" to "food",
      "amount" to 500L,
      "carryover" to true,
    )
  }

  private suspend fun BudgetSyncController.category(
    id: String,
    name: String,
    group: String,
    sortOrder: Long,
    hidden: Boolean = false,
    isIncome: Boolean = false,
  ) {
    row(
      CATEGORIES,
      id,
      "name" to name,
      "cat_group" to group,
      "sort_order" to sortOrder,
      "hidden" to hidden,
      "is_income" to isIncome,
    )
    row(CATEGORY_MAPPING, id, "transferId" to id)
  }

  private fun runQueriesTest(action: suspend BudgetDatabase.() -> Unit) =
    runSyncedDatabaseTest { scope, sync ->
      scheduler = scope.testScheduler
      sync.insertBudget()
      action()
    }

  private companion object {
    val TODAY = LocalDate(2026, 6, 15)
  }
}
