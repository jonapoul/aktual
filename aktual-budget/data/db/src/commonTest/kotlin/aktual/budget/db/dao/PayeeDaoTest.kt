package aktual.budget.db.dao

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.test.buildAccount
import aktual.budget.db.test.insertAccounts
import aktual.budget.db.withoutResult
import aktual.budget.model.AccountId
import aktual.budget.model.PayeeId
import aktual.budget.model.TransactionId
import aktual.test.runDatabaseTest
import assertk.assertThat
import assertk.assertions.containsExactly
import kotlin.test.Test
import kotlinx.datetime.LocalDate

internal class PayeeDaoTest {
  @Test
  fun `Common payees are the most used since the date`() = runDaoTest { payees, transactions ->
    // given
    transactions.insert("t1", ACCOUNT, null, "b", RECENT)
    transactions.insert("t2", ACCOUNT, null, "b", RECENT)
    transactions.insert("t3", ACCOUNT, null, "a", RECENT)
    transactions.insert("t4", ACCOUNT, null, "c", SINCE)
    transactions.insert("t5", ACCOUNT, null, "c", OLD)

    // then
    assertThat(payees.common(SINCE, limit = 10)).containsExactly(PayeeId("b"), PayeeId("a"))
  }

  @Test
  fun `Equally used common payees are in name order, up to the limit`() =
    runDaoTest { payees, transactions ->
      // given
      transactions.insert("t1", ACCOUNT, null, "c", RECENT)
      transactions.insert("t2", ACCOUNT, null, "b", RECENT)
      transactions.insert("t3", ACCOUNT, null, "a", RECENT)

      // then
      assertThat(payees.common(SINCE, limit = 2)).containsExactly(PayeeId("a"), PayeeId("b"))
    }

  @Test
  fun `Common payees leave out deleted transactions and payees, and transfers`() =
    runDaoTest { payees, transactions ->
      // given
      transactions.insert("t0", ACCOUNT, null, "a", OLD)
      insertDeletedCopy(transactions, id = "t1", copyOf = "t0", date = RECENT)
      transactions.insert("t2", ACCOUNT, null, DELETED, RECENT)
      transactions.insert("t3", ACCOUNT, null, TRANSFER, RECENT)
      transactions.insert("t4", ACCOUNT, null, "b", RECENT)

      // then
      assertThat(payees.common(SINCE, limit = 10)).containsExactly(PayeeId("b"))
    }

  private suspend fun BudgetDatabase.insertDeletedCopy(
    transactions: TransactionDao,
    id: String,
    copyOf: String,
    date: LocalDate,
  ) {
    val row = requireNotNull(transactions.row(TransactionId(copyOf)))
    transactionsQueries.withoutResult {
      insert(row.copy(id = TransactionId(id), date = date, tombstone = true))
    }
  }

  private fun runDaoTest(action: suspend BudgetDatabase.(PayeeDao, TransactionDao) -> Unit) =
    runDatabaseTest {
      insertAccounts(buildAccount(id = AccountId(ACCOUNT)))
      val payees = PayeeDao(this)
      payees.insert(PayeeId("a"), "Argos")
      payees.insert(PayeeId("b"), "B&Q")
      payees.insert(PayeeId("c"), "Co-op")
      payees.insert(PayeeId(TRANSFER), "Transfer", transferAccount = AccountId(ACCOUNT))
      // Inserted with its mapping, then deleted
      payees.insert(PayeeId(DELETED), "Deleted")
      payeesQueries.withoutResult {
        insert(
          id = PayeeId(DELETED),
          name = "Deleted",
          category = null,
          tombstone = true,
          transfer_acct = null,
          favorite = false,
          learn_categories = null,
        )
      }
      action(payees, TransactionDao(this))
    }

  private companion object {
    val OLD = LocalDate(2026, 1, 1)
    val SINCE = LocalDate(2026, 6, 1)
    val RECENT = LocalDate(2026, 7, 1)
    const val ACCOUNT = "account"
    const val TRANSFER = "transfer"
    const val DELETED = "deleted"
  }
}
