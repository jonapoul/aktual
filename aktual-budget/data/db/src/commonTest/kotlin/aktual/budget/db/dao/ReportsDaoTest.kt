package aktual.budget.db.dao

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.MonteCarloAccountBalances
import aktual.budget.db.test.buildAccount
import aktual.budget.db.test.insertAccounts
import aktual.budget.db.test.run
import aktual.budget.db.test.runDriverTest
import aktual.budget.model.AccountId
import aktual.test.assertThatNextEmissionIsEqualTo
import alakazam.test.TestCoroutineContexts
import app.cash.sqldelight.db.SqlDriver
import app.cash.turbine.test
import kotlin.test.Test
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.datetime.LocalDate

internal class ReportsDaoTest {
  private lateinit var driver: SqlDriver

  @Test
  fun `Monte Carlo balances include accounts with no transactions`() =
    runDaoTest { reports, transactions ->
      // given
      insertAccounts(
        buildAccount(id = AccountId("a")),
        buildAccount(id = AccountId("b")),
        buildAccount(id = AccountId("c")),
      )
      transactions.insert("t1", "a", "cat", "payee", LocalDate(2026, 1, 1), amount = 100.0)
      transactions.insert("t2", "a", "cat", "payee", LocalDate(2026, 2, 1), amount = 50.0)
      transactions.insert("t3", "c", "cat", "payee", LocalDate(2026, 2, 1), amount = 20.0)

      // then
      reports.observeMonteCarloAccountBalances(listOf(AccountId("a"), AccountId("b"))).test {
        assertThatNextEmissionIsEqualTo(
          listOf(
            MonteCarloAccountBalances(account = AccountId("a"), total = 15_000),
            MonteCarloAccountBalances(account = AccountId("b"), total = 0),
          ),
        )
      }
    }

  @Test
  fun `Monte Carlo balances count split parts, not parents, and skip deleted rows`() =
    runDaoTest { reports, transactions ->
      // given
      insertAccounts(buildAccount(id = AccountId("a")), buildAccount(id = AccountId("d")))
      driver.run("UPDATE accounts SET tombstone = 1 WHERE id = 'd'")
      transactions.insert("t1", "a", "cat", "payee", DATE, amount = 10.0)
      transactions.insert("t2", "a", "cat", "payee", DATE, amount = 1.0)
      transactions.insert("p", "a", "cat", "payee", DATE, amount = 100.0, isParent = true)
      transactions.insert("c1", "a", "cat", "payee", DATE, amount = 60.0, parent = "p")
      transactions.insert("c2", "a", "cat", "payee", DATE, amount = 40.0, parent = "p")
      transactions.insert("dead", "a", "cat", "payee", DATE, amount = 7.0, isParent = true)
      transactions.insert("orphan", "a", "cat", "payee", DATE, amount = 7.0, parent = "dead")
      transactions.insert("t3", "d", "cat", "payee", DATE, amount = 20.0)
      driver.run("UPDATE transactions SET tombstone = 1 WHERE id IN ('t2', 'dead')")

      // then
      reports.observeMonteCarloAccountBalances(listOf(AccountId("d"), AccountId("a"))).test {
        assertThatNextEmissionIsEqualTo(
          listOf(MonteCarloAccountBalances(account = AccountId("a"), total = 11_000)),
        )
      }
    }

  private fun runDaoTest(action: suspend BudgetDatabase.(ReportsDao, TransactionDao) -> Unit) =
    runDriverTest { driver, scope ->
      this@ReportsDaoTest.driver = driver
      val contexts = TestCoroutineContexts(StandardTestDispatcher(scope.testScheduler))
      action(ReportsDao(this, contexts), TransactionDao(this))
    }

  private companion object {
    val DATE = LocalDate(2026, 1, 1)
  }
}
