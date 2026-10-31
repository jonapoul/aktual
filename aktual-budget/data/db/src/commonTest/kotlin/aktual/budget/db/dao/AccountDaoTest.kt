package aktual.budget.db.dao

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.test.buildAccount
import aktual.budget.db.test.insertAccounts
import aktual.budget.db.test.run
import aktual.budget.db.test.runDriverTest
import aktual.budget.db.withoutResult
import aktual.budget.model.AccountId
import aktual.budget.model.TransactionId
import aktual.test.assertThatNextEmissionIsEqualTo
import app.cash.sqldelight.db.SqlDriver
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import kotlin.test.Test
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate

internal class AccountDaoTest {
  private lateinit var driver: SqlDriver

  @Test
  fun `Balances count split parents once and skip deleted transactions`() =
    runDaoTest { accounts, transactions ->
      // given
      insertAccounts(buildAccount(id = AccountId("a")))
      transactions.insert("t1", "a", "cat", "payee", DATE, amount = 10.0)
      transactions.insert("p", "a", "cat", "payee", DATE, amount = 100.0, isParent = true)
      transactions.insert("c1", "a", "cat", "payee", DATE, amount = 60.0, parent = "p")
      transactions.insert("c2", "a", "cat", "payee", DATE, amount = 40.0, parent = "p")
      insertDeleted(transactions, id = "t2", copyOf = "t1")

      // then
      accounts.observeBalances().test { assertThatNextEmissionIsEqualTo(listOf("a" to 11_000L)) }
    }

  @Test
  fun `Balances include future transactions`() = runDaoTest { accounts, transactions ->
    // given
    insertAccounts(buildAccount(id = AccountId("a")))
    transactions.insert("t1", "a", "cat", "payee", DATE, amount = 10.0)
    transactions.insert("t2", "a", "cat", "payee", LocalDate(2999, 1, 1), amount = 5.0)

    // then
    accounts.observeBalances().test { assertThatNextEmissionIsEqualTo(listOf("a" to 1_500L)) }
  }

  @Test
  fun `Balances cover closed, off budget and empty accounts`() =
    runDaoTest { accounts, transactions ->
      // given
      insertAccounts(
        buildAccount(id = AccountId("a"), name = "A"),
        buildAccount(id = AccountId("b"), name = "B", offBudget = true),
        buildAccount(id = AccountId("c"), name = "C"),
        buildAccount(id = AccountId("d"), name = "D"),
      )
      accountsQueries.withoutResult { closeAccount(AccountId("c")) }
      transactions.insert("t1", "a", "cat", "payee", DATE, amount = 10.0)
      transactions.insert("t2", "b", "cat", "payee", DATE, amount = -20.0)
      transactions.insert("t3", "c", "cat", "payee", DATE, amount = 30.0)

      // then
      accounts.observeAllWithBalances().test {
        val rows = awaitItem()
        assertThat(rows.map { it.id.value to it.balance })
          .containsExactly("a" to 1_000L, "b" to -2_000L, "c" to 3_000L, "d" to 0L)
        assertThat(rows.map { it.offbudget }).containsExactly(false, true, false, false)
        assertThat(rows.map { it.closed }).containsExactly(false, false, true, false)
        assertThat(rows.map { it.last_activity }).containsExactly(DATE, DATE, DATE, null)
      }
    }

  @Test
  fun `Balances re-emit when a transaction is inserted`() = runDaoTest { accounts, transactions ->
    // given
    insertAccounts(buildAccount(id = AccountId("a")))

    accounts.observeBalances().test {
      assertThatNextEmissionIsEqualTo(listOf("a" to 0L))

      // when
      transactions.insert("t1", "a", "cat", "payee", DATE, amount = 12.34)

      // then
      assertThatNextEmissionIsEqualTo(listOf("a" to 1_234L))
    }
  }

  @Test
  fun `Last activity is the latest transaction date`() = runDaoTest { accounts, transactions ->
    // given
    insertAccounts(buildAccount(id = AccountId("a")))
    transactions.insert("t1", "a", "cat", "payee", LocalDate(2026, 2, 3), amount = 10.0)
    transactions.insert("t2", "a", "cat", "payee", DATE, amount = 5.0)

    // then
    accounts.observeAllWithBalances().test {
      assertThat(awaitItem().map { it.last_activity }).containsExactly(LocalDate(2026, 2, 3))
    }
  }

  @Test
  fun `Deleted accounts aren't listed and the rest are in sort order`() =
    runDaoTest { accounts, transactions ->
      // given
      insertAccounts(
        buildAccount(id = AccountId("a"), name = "A"),
        buildAccount(id = AccountId("b"), name = "B"),
        buildAccount(id = AccountId("c"), name = "C"),
        buildAccount(id = AccountId("d"), name = "D"),
      )
      driver.run("UPDATE accounts SET sort_order = 2 WHERE id IN ('a', 'b')")
      driver.run("UPDATE accounts SET sort_order = 1 WHERE id = 'c'")
      driver.run("UPDATE accounts SET tombstone = 1 WHERE id = 'd'")
      transactions.insert("t1", "a", "cat", "payee", DATE, amount = 10.0)
      transactions.insert("t2", "d", "cat", "payee", DATE, amount = 20.0)

      // then
      accounts.observeBalances().test {
        assertThatNextEmissionIsEqualTo(listOf("c" to 0L, "a" to 1_000L, "b" to 0L))
      }
    }

  @Test
  fun `Last activity skips deleted transactions and children of deleted splits`() =
    runDaoTest { accounts, transactions ->
      // given
      insertAccounts(buildAccount(id = AccountId("a")))
      transactions.insert("t1", "a", "cat", "payee", DATE, amount = 10.0)
      transactions.insert("t2", "a", "cat", "payee", LocalDate(2026, 3, 1), amount = 5.0)
      transactions.insert("p", "a", "cat", "payee", LocalDate(2026, 4, 1), isParent = true)
      transactions.insert("c", "a", "cat", "payee", LocalDate(2026, 5, 1), parent = "p")
      driver.run("UPDATE transactions SET tombstone = 1 WHERE id IN ('t2', 'p')")

      // then
      accounts.observeAllWithBalances().test {
        val rows = awaitItem()
        assertThat(rows.map { it.last_activity }).containsExactly(DATE)
        assertThat(rows.map { it.balance }).containsExactly(1_000L)
      }
    }

  private fun AccountDao.observeBalances() =
    observeAllWithBalances().map { rows -> rows.map { it.id.value to it.balance } }

  private suspend fun BudgetDatabase.insertDeleted(
    transactions: TransactionDao,
    id: String,
    copyOf: String,
  ) {
    val row = requireNotNull(transactions.row(TransactionId(copyOf)))
    transactionsQueries.withoutResult { insert(row.copy(id = TransactionId(id), tombstone = true)) }
  }

  private fun runDaoTest(action: suspend BudgetDatabase.(AccountDao, TransactionDao) -> Unit) =
    runDriverTest { driver, _ ->
      this@AccountDaoTest.driver = driver
      action(AccountDao(this), TransactionDao(this))
    }

  private companion object {
    val DATE = LocalDate(2026, 1, 1)
  }
}
