package aktual.budget.db.dao

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.test.buildAccount
import aktual.budget.db.test.insertAccounts
import aktual.budget.db.test.run
import aktual.budget.db.test.runDriverTest
import aktual.budget.db.withoutResult
import aktual.budget.model.AccountId
import aktual.budget.model.BudgetType
import aktual.budget.model.CategoryId
import aktual.budget.model.PayeeId
import alakazam.test.TestCoroutineContexts
import app.cash.sqldelight.db.SqlDriver
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import kotlin.test.Test
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth

internal class BudgetDaoTest {
  private lateinit var driver: SqlDriver

  @Test
  fun `First activity is the earliest budget row or transaction`() =
    runDaoTest { budgets, transactions ->
      // given
      insertBudget("zero_budgets", "z1", month = 202403, CATEGORY)
      insertBudget("zero_budgets", "z2", month = 202301, OTHER)
      insertBudget("reflect_budgets", "r1", month = 202401, CATEGORY)
      insertBudget("reflect_budgets", "r2", month = 202501, CATEGORY)
      transactions.insert("t1", ON, CATEGORY, PAYEE, LocalDate(2024, 2, 10))

      // then
      assertThat(budgets.firstActivity(Envelope)).isEqualTo(YearMonth(2024, 2))
      assertThat(budgets.firstActivity(Tracking)).isEqualTo(YearMonth(2024, 1))
    }

  @Test
  fun `First activity skips off budget, deleted, split parent and other category transactions`() =
    runDaoTest { budgets, transactions ->
      // given
      transactions.insert("off", OFF, CATEGORY, PAYEE, LocalDate(2023, 1, 1))
      transactions.insert("deleted", ON, CATEGORY, PAYEE, LocalDate(2023, 2, 1))
      delete("deleted")
      transactions.insert("parent", ON, CATEGORY, PAYEE, LocalDate(2023, 3, 1), isParent = true)
      transactions.insert("dead", ON, null, PAYEE, LocalDate(2023, 4, 1), isParent = true)
      delete("dead")
      transactions.insert("orphan", ON, CATEGORY, PAYEE, LocalDate(2023, 4, 1), parent = "dead")
      transactions.insert("other", ON, OTHER, PAYEE, LocalDate(2023, 5, 1))
      transactions.insert("t1", ON, CATEGORY, PAYEE, LocalDate(2023, 6, 1))

      // then
      assertThat(budgets.firstActivity(Envelope)).isEqualTo(YearMonth(2023, 6))
      assertThat(budgets.firstActivity(Tracking)).isEqualTo(YearMonth(2023, 6))
    }

  @Test
  fun `First activity counts split parts`() = runDaoTest { budgets, transactions ->
    // given
    transactions.insert("p", ON, null, PAYEE, LocalDate(2023, 8, 1), isParent = true)
    transactions.insert("c", ON, CATEGORY, PAYEE, LocalDate(2023, 8, 1), parent = "p")

    // then
    assertThat(budgets.firstActivity(Envelope)).isEqualTo(YearMonth(2023, 8))
    assertThat(budgets.firstActivity(Tracking)).isEqualTo(YearMonth(2023, 8))
  }

  @Test
  fun `First activity follows merged categories`() = runDaoTest { budgets, transactions ->
    // given a category merged into CATEGORY, whose transactions now count for it
    CategoryDao(this).insert(CategoryId(MERGED), "Merged")
    categoryMappingQueries.withoutResult {
      insert(id = CategoryId(MERGED), transferId = CategoryId(CATEGORY))
    }
    transactions.insert("t1", ON, MERGED, PAYEE, LocalDate(2023, 7, 1))

    // then
    assertThat(budgets.firstActivity(Envelope)).isEqualTo(YearMonth(2023, 7))
    assertThat(budgets.firstActivity(Envelope, category = MERGED)).isNull()
  }

  @Test
  fun `First activity covers the whole end month and nothing after it`() =
    runDaoTest { budgets, transactions ->
      // given
      transactions.insert("t1", ON, CATEGORY, PAYEE, LocalDate(2024, 6, 30))
      transactions.insert("t2", ON, CATEGORY, PAYEE, LocalDate(2024, 7, 1))

      // then
      assertThat(budgets.firstActivity(Envelope, end = YearMonth(2024, 6)))
        .isEqualTo(YearMonth(2024, 6))
      assertThat(budgets.firstActivity(Tracking, end = YearMonth(2024, 6)))
        .isEqualTo(YearMonth(2024, 6))
      assertThat(budgets.firstActivity(Envelope, end = YearMonth(2024, 5))).isNull()
      assertThat(budgets.firstActivity(Tracking, end = YearMonth(2024, 5))).isNull()
    }

  private suspend fun BudgetDao.firstActivity(
    type: BudgetType,
    category: String = CATEGORY,
    end: YearMonth = END,
  ): YearMonth? = firstActivityMonth(type, CategoryId(category), end)

  private suspend fun insertBudget(table: String, id: String, month: Int, category: String) =
    driver.run(
      "INSERT INTO $table(id, month, category, amount) VALUES ('$id', $month, '$category', 100)",
    )

  private suspend fun delete(id: String) =
    driver.run("UPDATE transactions SET tombstone = 1 WHERE id = '$id'")

  private fun runDaoTest(action: suspend BudgetDatabase.(BudgetDao, TransactionDao) -> Unit) =
    runDriverTest { driver, scope ->
      this@BudgetDaoTest.driver = driver
      insertAccounts(
        buildAccount(id = AccountId(ON)),
        buildAccount(id = AccountId(OFF), name = "Mortgage", offBudget = true),
      )
      PayeeDao(this).insert(PayeeId(PAYEE), "Payee")
      CategoryDao(this).insert(CategoryId(CATEGORY), "Category")
      CategoryDao(this).insert(CategoryId(OTHER), "Other")
      val contexts = TestCoroutineContexts(StandardTestDispatcher(scope.testScheduler))
      action(BudgetDao(this, contexts), TransactionDao(this))
    }

  private companion object {
    val END = YearMonth(2024, 12)
    const val ON = "on"
    const val OFF = "off"
    const val PAYEE = "payee"
    const val CATEGORY = "category"
    const val OTHER = "other"
    const val MERGED = "merged"
  }
}
