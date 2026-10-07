package aktual.budget.db.dao

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.MonteCarloAccountBalances
import aktual.budget.db.test.buildAccount
import aktual.budget.db.test.insertAccounts
import aktual.budget.model.AccountId
import aktual.test.assertThatNextEmissionIsEqualTo
import aktual.test.runDatabaseTest
import alakazam.test.TestCoroutineContexts
import app.cash.turbine.test
import kotlin.test.Test
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.datetime.LocalDate

internal class ReportsDaoTest {
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

  private fun runDaoTest(action: suspend BudgetDatabase.(ReportsDao, TransactionDao) -> Unit) =
    runDatabaseTest { scope ->
      val contexts = TestCoroutineContexts(StandardTestDispatcher(scope.testScheduler))
      action(ReportsDao(this, contexts), TransactionDao(this))
    }
}
