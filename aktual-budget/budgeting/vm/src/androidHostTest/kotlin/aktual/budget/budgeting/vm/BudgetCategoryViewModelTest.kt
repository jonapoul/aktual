package aktual.budget.budgeting.vm

import aktual.budget.budgeting.domain.BudgetMonthCalculatorImpl
import aktual.budget.db.BudgetDatabase
import aktual.budget.db.buildDatabase
import aktual.budget.db.dao.BudgetDao
import aktual.budget.db.dao.PreferencesDao
import aktual.budget.model.Amount
import aktual.budget.model.BudgetId
import aktual.budget.model.CategoryId
import aktual.test.TestCalendar
import aktual.test.inMemoryDriverFactory
import alakazam.test.TestCoroutineContexts
import app.cash.sqldelight.db.SqlDriver
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class BudgetCategoryViewModelTest {
  private val calendar = TestCalendar(LocalDate(2026, 4, 15))

  @AfterTest
  fun after() {
    Dispatchers.resetMain()
  }

  @Test
  fun `History matches each month's budget`() = runCategoryTest { viewModel, calculator ->
    viewModel.state.test {
      val state = awaitLoaded()
      assertThat(state.name).isEqualTo("Food")
      assertThat(state.group).isEqualTo("Usual")
      assertThat(state.selected).isEqualTo(APRIL)
      assertThat(state.current).isEqualTo(APRIL)

      // Bounds start three months before the earliest transaction
      assertThat(state.history.map { it.month })
        .containsExactly(
          YearMonth(2025, 12),
          YearMonth(2026, 1),
          YearMonth(2026, 2),
          YearMonth(2026, 3),
          APRIL,
        )
      for (point in state.history) {
        val food = calculator.observe(point.month).first().categories.first { it.id == FOOD }
        assertThat(point)
          .isEqualTo(
            CategoryHistoryMonth(
              month = point.month,
              budgeted = food.budgeted,
              spent = food.spent,
              balance = food.balance,
              carryover = food.carryover,
            ),
          )
      }
      assertThat(state.selectedMonth?.spent).isEqualTo(Amount(-50_000L))
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Selects a month in the history`() = runCategoryTest { viewModel, _ ->
    viewModel.state.test {
      awaitLoaded()

      viewModel.select(MARCH)
      val state = awaitLoaded()
      assertThat(state.selected).isEqualTo(MARCH)
      assertThat(state.selectedMonth?.budgeted).isEqualTo(Amount(4_000L))

      // Outside the history, so nothing changes
      viewModel.select(YearMonth(2027, 1))
      expectNoEvents()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Unknown category fails`() =
    runCategoryTest(category = CategoryId("gone")) { viewModel, _ ->
      viewModel.state.test {
        assertThat(awaitItem()).isEqualTo(Loading)
        assertThat(awaitItem()).isEqualTo(Failed)
        cancelAndIgnoreRemainingEvents()
      }
    }

  private suspend fun ReceiveTurbine<BudgetCategoryState>.awaitLoaded():
    BudgetCategoryState.Loaded {
    var item = awaitItem()
    while (item !is Loaded) item = awaitItem()
    assertThat(item).isInstanceOf<BudgetCategoryState.Loaded>()
    return item
  }

  private fun runCategoryTest(
    category: CategoryId = FOOD,
    action: suspend TestScope.(BudgetCategoryViewModel, BudgetMonthCalculatorImpl) -> Unit,
  ) = runTest {
    val driver = inMemoryDriverFactory().create(BudgetId("abc-123"))
    driver.use {
      val database = buildDatabase(driver)
      SETUP.forEach { sql -> driver.run(sql) }
      Dispatchers.setMain(StandardTestDispatcher(testScheduler))
      val calculator = database.calculator(this)
      action(BudgetCategoryViewModel(category, APRIL, calculator, calendar), calculator)
    }
  }

  private fun BudgetDatabase.calculator(scope: TestScope): BudgetMonthCalculatorImpl {
    val contexts = TestCoroutineContexts(StandardTestDispatcher(scope.testScheduler))
    return BudgetMonthCalculatorImpl(
      budgetDao = BudgetDao(this, contexts),
      preferencesDao = PreferencesDao(this, contexts),
      calendar = calendar,
      contexts = contexts,
    )
  }

  private suspend fun SqlDriver.run(sql: String) {
    execute(identifier = null, sql = sql, parameters = 0).await()
  }

  private companion object {
    val FOOD = CategoryId("food")
    val MARCH = YearMonth(2026, 3)
    val APRIL = YearMonth(2026, 4)

    @Suppress("MaxLineLength")
    val SETUP =
      listOf(
        "INSERT INTO category_groups(id, name, is_income, sort_order) VALUES ('usual', 'Usual', 0, 1)",
        "INSERT INTO category_groups(id, name, is_income, sort_order) VALUES ('income', 'Income', 1, 3)",
        "INSERT INTO categories(id, name, is_income, cat_group, sort_order) VALUES ('food', 'Food', 0, 'usual', 1)",
        "INSERT INTO categories(id, name, is_income, cat_group, sort_order) VALUES ('salary', 'Salary', 1, 'income', 1)",
        "INSERT INTO category_mapping(id, transferId) VALUES ('food', 'food'), ('salary', 'salary')",
        "INSERT INTO accounts(id, name, offbudget) VALUES ('on', 'On', 0)",
        "INSERT INTO transactions(id, acct, category, amount, date) VALUES ('t1', 'on', 'salary', 300000, 20260301)",
        "INSERT INTO transactions(id, acct, category, amount, date) VALUES ('t2', 'on', 'food', -3000, 20260302)",
        "INSERT INTO transactions(id, acct, category, amount, date) VALUES ('t3', 'on', 'food', -50000, 20260402)",
        "INSERT INTO zero_budgets(id, month, category, amount) VALUES ('202603-food', 202603, 'food', 4000), ('202604-food', 202604, 'food', 5000)",
      )
  }
}
