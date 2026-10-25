package aktual.budget.db.dao

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.GetPayeeList
import aktual.budget.db.test.buildAccount
import aktual.budget.db.test.insertAccounts
import aktual.budget.db.test.insertPayeeMapping
import aktual.budget.db.withoutResult
import aktual.budget.model.AccountId
import aktual.budget.model.PayeeId
import aktual.budget.model.TransactionId
import aktual.test.runDatabaseTest
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.extracting
import assertk.assertions.isEqualTo
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

  @Test
  fun `List names transfer payees after their account and leaves out deleted payees`() =
    runDaoTest { payees, _ ->
      // then
      assertThat(payees.list())
        .extracting(GetPayeeList::id, GetPayeeList::name, GetPayeeList::transfer_acct)
        .containsExactly(
          Triple(PayeeId("a"), "Argos", null),
          Triple(PayeeId("b"), "B&Q", null),
          Triple(PayeeId("c"), "Co-op", null),
          Triple(PayeeId(TRANSFER), ACCOUNT_NAME, AccountId(ACCOUNT)),
        )
    }

  @Test
  fun `List leaves out transfer payees without an alive account`() = runDaoTest { payees, _ ->
    // given an account that doesn't exist, which the join treats the same as a deleted one
    payees.insert(PayeeId("orphan-transfer"), null, transferAccount = AccountId("missing"))

    // then
    assertThat(payees.list())
      .extracting(GetPayeeList::id)
      .containsExactly(PayeeId("a"), PayeeId("b"), PayeeId("c"), PayeeId(TRANSFER))
  }

  @Test
  fun `List counts alive transactions through the payee mapping, with each split once`() =
    runDaoTest { payees, transactions ->
      // given
      transactions.insert("t1", ACCOUNT, null, "a", RECENT)
      transactions.insert("t2", ACCOUNT, null, "a", RECENT)
      insertDeletedCopy(transactions, id = "t3", copyOf = "t1", date = RECENT)
      // A split with both parts on the same payee
      transactions.insert("split", ACCOUNT, null, "b", RECENT, isParent = true)
      transactions.insert("part1", ACCOUNT, null, "b", RECENT, parent = "split")
      transactions.insert("part2", ACCOUNT, null, "b", RECENT, parent = "split")
      // Merged into c, so its transactions count towards c
      payees.insert(PayeeId("merged"), "Merged")
      insertPayeeMapping("merged", "c")
      transactions.insert("t4", ACCOUNT, null, "merged", RECENT)

      // then
      assertThat(payees.list().associate { it.id to it.num_transactions })
        .isEqualTo(
          mapOf(
            PayeeId("a") to 2L,
            PayeeId("b") to 1L,
            PayeeId("c") to 1L,
            PayeeId("merged") to 0L,
            PayeeId(TRANSFER) to 0L,
          ),
        )
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
      insertAccounts(buildAccount(id = AccountId(ACCOUNT), name = ACCOUNT_NAME))
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
    const val ACCOUNT_NAME = "Current"
    const val TRANSFER = "transfer"
    const val DELETED = "deleted"
  }
}
