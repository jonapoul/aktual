package aktual.budget.budgeting.domain

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.buildDatabase
import aktual.budget.db.dao.BudgetDao
import aktual.budget.db.dao.PreferencesDao
import aktual.budget.db.dao.TransactionDao
import aktual.budget.model.Amount
import aktual.budget.model.BudgetId
import aktual.budget.model.SyncedPrefKey
import aktual.test.assertThatNextEmission
import aktual.test.assertThatNextEmissionIsEqualTo
import aktual.test.inMemoryDriverFactory
import alakazam.test.TestCoroutineContexts
import app.cash.sqldelight.db.SqlDriver
import app.cash.turbine.test
import assertk.all
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.prop
import kotlin.test.Test
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth

internal class BudgetMonthCalculatorTest {
  @Test
  fun `Envelope month is built from the database`() = runCalculatorTest { calculator, _ ->
    calculator.observe(YearMonth(2026, 4)).test {
      assertThatNextEmission().isInstanceOf<BudgetMonth.Envelope>().all {
        prop(BudgetMonth.Envelope::fromLastMonth).isEqualTo(Amount(260_000L))
        prop(BudgetMonth.Envelope::toBudget).isEqualTo(Amount(230_000L))
        prop(BudgetMonth.Envelope::budgeted).isEqualTo(Amount(30_000L))
        prop(BudgetMonth.Envelope::spent).isEqualTo(Amount(-50_000L))
        prop(BudgetMonth.Envelope::buffered).isEqualTo(Zero)
        transform { month -> month.categories.map { it.name to it.balance } }
          .containsExactly("Food" to Amount(-15_000L), "Salary" to Amount.Zero)
      }
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Buffered amount is read from the budget month`() = runCalculatorTest { calculator, _ ->
    calculator.observe(YearMonth(2026, 3)).test {
      assertThatNextEmission().isInstanceOf<BudgetMonth.Envelope>().all {
        prop(BudgetMonth.Envelope::income).isEqualTo(Amount(300_000L))
        prop(BudgetMonth.Envelope::buffered).isEqualTo(Amount(100_000L))
        prop(BudgetMonth.Envelope::toBudget).isEqualTo(Amount(160_000L))
      }
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Emits again when a transaction is added`() = runCalculatorTest { calculator, database ->
    calculator.observe(YearMonth(2026, 4)).test {
      assertThatNextEmission().prop(BudgetMonth::spent).isEqualTo(Amount(-50_000L))

      TransactionDao(database)
        .insert("t5", "on", "food", "payee", LocalDate(2026, 4, 3), amount = -10.0)

      assertThatNextEmission().all {
        prop(BudgetMonth::spent).isEqualTo(Amount(-51_000L))
        prop(BudgetMonth::balance).isEqualTo(Amount(-16_000L))
      }
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Switches to tracking when the budget type changes`() =
    runCalculatorTest { calculator, database ->
      calculator.observe(YearMonth(2026, 3)).test {
        assertThatNextEmission().isInstanceOf<BudgetMonth.Envelope>()

        database.preferences(this@runCalculatorTest)[SyncedPrefKey.Global.BudgetType] = "tracking"

        assertThatNextEmission().isInstanceOf<BudgetMonth.Tracking>().all {
          prop(BudgetMonth.Tracking::budgeted).isEqualTo(Amount(45_000L))
          prop(BudgetMonth.Tracking::spent).isEqualTo(Amount(-35_000L))
          prop(BudgetMonth.Tracking::income).isEqualTo(Amount(300_000L))
          prop(BudgetMonth.Tracking::incomeBudgeted).isEqualTo(Amount(280_000L))
        }
        cancelAndIgnoreRemainingEvents()
      }
    }

  @Test
  fun `Envelope range matches each month on its own`() = runCalculatorTest { calculator, _ ->
    assertRangeMatchesMonths(calculator)
  }

  @Test
  fun `Tracking range matches each month on its own`() = runCalculatorTest { calculator, database ->
    database.preferences(this)[SyncedPrefKey.Global.BudgetType] = "tracking"
    assertRangeMatchesMonths(calculator)
  }

  @Test
  fun `Months are grouped with group names`() = runCalculatorTest { calculator, _ ->
    calculator.observe(YearMonth(2026, 4)).test {
      assertThatNextEmission()
        .transform { month -> month.groups.map { it.name to it.categories.map { c -> c.name } } }
        .containsExactly(
          "Usual" to listOf("Food"),
          "Empty" to emptyList<String>(),
          "Income" to listOf("Salary"),
        )
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Bounds follow the earliest transaction`() = runCalculatorTest { calculator, database ->
    calculator.observeBounds().test {
      assertThatNextEmissionIsEqualTo(YearMonth(2025, 12)..YearMonth(2027, 4))

      TransactionDao(database)
        .insert("t8", "on", "food", "payee", LocalDate(2025, 6, 3), amount = -10.0)

      assertThatNextEmissionIsEqualTo(YearMonth(2025, 3)..YearMonth(2027, 4))
      cancelAndIgnoreRemainingEvents()
    }
  }

  private suspend fun TestScope.assertRangeMatchesMonths(calculator: BudgetMonthCalculator) {
    val range = YearMonth(2026, 1)..YearMonth(2026, 5)
    val single = range.map { month -> calculator.observe(month).first() }.toImmutableList()
    calculator.observeRange(range).test {
      assertThatNextEmissionIsEqualTo(single)
      cancelAndIgnoreRemainingEvents()
    }
  }

  private fun runCalculatorTest(
    action: suspend TestScope.(BudgetMonthCalculator, BudgetDatabase) -> Unit,
  ) = runTest {
    val driver = inMemoryDriverFactory().create(BudgetId("abc-123"))
    driver.use {
      val database = buildDatabase(driver)
      SETUP.forEach { sql -> driver.run(sql) }
      val contexts = TestCoroutineContexts(StandardTestDispatcher(testScheduler))
      val calculator =
        BudgetMonthCalculatorImpl(
          budgetDao = BudgetDao(database, contexts),
          preferencesDao = database.preferences(this),
          calendar = { TODAY },
          contexts = contexts,
        )
      action(calculator, database)
    }
  }

  private fun BudgetDatabase.preferences(scope: TestScope) =
    PreferencesDao(this, TestCoroutineContexts(StandardTestDispatcher(scope.testScheduler)))

  private suspend fun SqlDriver.run(sql: String) {
    execute(identifier = null, sql = sql, parameters = 0).await()
  }

  private companion object {
    val TODAY = LocalDate(2026, 4, 15)

    // The off budget, deleted and uncategorised transactions don't count
    @Suppress("MaxLineLength", "TrimMultilineRawString")
    val SETUP =
      listOf(
        "INSERT INTO category_groups(id, name, is_income, sort_order) VALUES ('usual', 'Usual', 0, 1)",
        "INSERT INTO category_groups(id, name, is_income, sort_order) VALUES ('income', 'Income', 1, 2)",
        "INSERT INTO category_groups(id, name, is_income, sort_order) VALUES ('empty', 'Empty', 0, 3)",
        "INSERT INTO category_groups(id, name, is_income, sort_order, tombstone) VALUES ('gone', 'Gone', 0, 4, 1)",
        "INSERT INTO categories(id, name, is_income, cat_group, sort_order) VALUES ('food', 'Food', 0, 'usual', 1)",
        "INSERT INTO categories(id, name, is_income, cat_group, sort_order) VALUES ('salary', 'Salary', 1, 'income', 1)",
        "INSERT INTO categories(id, name, is_income, cat_group, tombstone) VALUES ('old', 'Old', 0, 'usual', 1)",
        "INSERT INTO category_mapping(id, transferId) VALUES ('food', 'food'), ('salary', 'salary')",
        "INSERT INTO accounts(id, name, offbudget) VALUES ('on', 'On', 0), ('off', 'Off', 1)",
        """
        INSERT INTO transactions(id, acct, category, amount, date, tombstone) VALUES
          ('t1', 'on', 'salary', 300000, 20260305, 0),
          ('t2', 'on', 'food', -35000, 20260310, 0),
          ('t3', 'off', 'food', -9900, 20260311, 0),
          ('t4', 'on', 'food', -50000, 20260402, 0),
          ('t6', 'on', 'food', -7700, 20260403, 1),
          ('t7', 'on', NULL, -1200, 20260404, 0)
        """,
        """
        INSERT INTO zero_budgets(id, month, category, amount) VALUES
          ('202603-food', 202603, 'food', 40000),
          ('202604-food', 202604, 'food', 30000),
          ('202604-old', 202604, 'old', 99900)
        """,
        """
        INSERT INTO reflect_budgets(id, month, category, amount) VALUES
          ('202603-food', 202603, 'food', 45000),
          ('202603-salary', 202603, 'salary', 280000)
        """,
        "INSERT INTO zero_budget_months(id, buffered) VALUES ('2026-03', 100000)",
      )
  }
}
