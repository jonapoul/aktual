package aktual.budget.db.dao

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.Categories
import aktual.budget.db.Transactions
import aktual.budget.db.test.buildAccount
import aktual.budget.db.test.insertAccounts
import aktual.budget.db.withoutResult
import aktual.budget.model.AccountId
import aktual.budget.model.CategoryId
import aktual.budget.model.PayeeId
import aktual.budget.model.TransactionId
import aktual.test.assertThatNextEmissionIsEqualTo
import aktual.test.runDatabaseTest
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlinx.datetime.LocalDate

internal class TransactionDaoTest {
  @Test
  fun `Categorised transactions aren't uncategorised`() = runDaoTest { transactions ->
    // given
    transactions.insert("t1", ON_1, CATEGORY, PAYEE, DATE)
    transactions.insert("t2", ON_1, category = null, PAYEE, DATE)

    // then
    assertThat(transactions.uncategorisedIds()).containsExactly("t2")
  }

  @Test
  fun `Transfers between on budget accounts aren't uncategorised`() = runDaoTest { transactions ->
    // given
    transactions.insert("t1", ON_1, category = null, TRANSFER_TO_ON_2, DATE)

    // then
    assertThat(transactions.uncategorisedIds()).containsExactly()
  }

  @Test
  fun `Transfers to off budget accounts are uncategorised`() = runDaoTest { transactions ->
    // given
    transactions.insert("t1", ON_1, category = null, TRANSFER_TO_OFF, DATE)

    // then
    assertThat(transactions.uncategorisedIds()).containsExactly("t1")
  }

  @Test
  fun `Transactions in off budget accounts aren't uncategorised`() = runDaoTest { transactions ->
    // given
    transactions.insert("t1", OFF, category = null, PAYEE, DATE)
    transactions.insert("t2", OFF, category = null, TRANSFER_TO_ON_1, DATE)

    // then
    assertThat(transactions.uncategorisedIds()).containsExactly()
  }

  @Test
  fun `Split children are uncategorised but their parents aren't`() = runDaoTest { transactions ->
    // given
    transactions.insert("p", ON_1, category = null, PAYEE, DATE, isParent = true)
    transactions.insert("c1", ON_1, category = null, PAYEE, DATE, parent = "p")
    transactions.insert("c2", ON_1, CATEGORY, PAYEE, DATE, parent = "p")

    // then
    assertThat(transactions.uncategorisedIds()).containsExactly("c1")
  }

  @Test
  fun `Starting balances and deleted transactions aren't uncategorised`() =
    runDaoTest { transactions ->
      // given
      transactions.insert("t1", ON_1, category = null, PAYEE, DATE)
      insertCopy(transactions, id = "t2", copyOf = "t1") { it.copy(starting_balance_flag = true) }
      insertCopy(transactions, id = "t3", copyOf = "t1") { it.copy(tombstone = true) }

      // then
      assertThat(transactions.uncategorisedIds()).containsExactly("t1")
    }

  @Test
  fun `Transactions with a deleted category are uncategorised`() = runDaoTest { transactions ->
    // given
    transactions.insert("t1", ON_1, DELETED_CATEGORY, PAYEE, DATE)

    // then
    assertThat(transactions.uncategorisedIds()).containsExactly("t1")
  }

  @Test
  fun `Uncategorised transactions of one account are paged newest first`() =
    runDaoTest { transactions ->
      // given
      transactions.insert("t1", ON_1, category = null, PAYEE, DATE)
      transactions.insert("t2", ON_2, category = null, PAYEE, DATE)
      transactions.insert("t3", ON_1, category = null, PAYEE, LocalDate(2026, 1, 2))
      transactions.insert("t4", ON_1, category = null, PAYEE, LocalDate(2026, 1, 3))

      // when
      val first = transactions.getUncategorisedPaged(AccountId(ON_1), limit = 2, offset = 0)
      val second = transactions.getUncategorisedPaged(AccountId(ON_1), limit = 2, offset = 2)

      // then
      assertThat(first.map { it.id.toString() }).containsExactly("t4", "t3")
      assertThat(second.map { it.id.toString() }).containsExactly("t1")
    }

  @Test
  fun `Rows of the whole list only need a category if they're uncategorised`() =
    runDaoTest { transactions ->
      // given
      transactions.insert("t1", ON_1, CATEGORY, PAYEE, DATE)
      transactions.insert("t2", ON_1, category = null, PAYEE, DATE)
      transactions.insert("t3", ON_1, category = null, TRANSFER_TO_ON_2, DATE)
      transactions.insert("t4", OFF, category = null, PAYEE, DATE)
      transactions.insert("t5", ON_1, category = null, PAYEE, DATE, isParent = true)

      // when
      val rows = transactions.getPaged(limit = 10, offset = 0).rows

      // then
      assertThat(rows.associate { it.id.toString() to it.needsCategory })
        .isEqualTo(mapOf("t1" to false, "t2" to true, "t3" to false, "t4" to false, "t5" to false))
    }

  @Test
  fun `Uncategorised count re-emits as transactions change`() = runDaoTest { transactions ->
    transactions.observeUncategorisedCount().test {
      assertThatNextEmissionIsEqualTo(0L)

      // when
      transactions.insert("t1", ON_1, category = null, PAYEE, DATE)

      // then
      assertThatNextEmissionIsEqualTo(1L)

      // when
      transactions.insert("t2", ON_1, CATEGORY, PAYEE, DATE)
      transactions.insert("t3", ON_2, category = null, PAYEE, DATE)

      // then
      assertThatNextEmissionIsEqualTo(2L)
    }
  }

  private suspend fun TransactionDao.uncategorisedIds(): List<String> =
    getUncategorisedPaged(account = null, limit = 10, offset = 0).map { it.id.toString() }

  private suspend fun BudgetDatabase.insertCopy(
    transactions: TransactionDao,
    id: String,
    copyOf: String,
    change: (Transactions) -> Transactions,
  ) {
    val row = requireNotNull(transactions.row(TransactionId(copyOf)))
    transactionsQueries.withoutResult { insert(change(row.copy(id = TransactionId(id)))) }
  }

  private fun runDaoTest(action: suspend BudgetDatabase.(TransactionDao) -> Unit) =
    runDatabaseTest {
      insertAccounts(
        buildAccount(id = AccountId(ON_1)),
        buildAccount(id = AccountId(ON_2)),
        buildAccount(id = AccountId(OFF), offBudget = true),
      )
      PayeeDao(this).insert(PayeeId(PAYEE), "Payee")
      insertTransferPayee(TRANSFER_TO_ON_1, ON_1)
      insertTransferPayee(TRANSFER_TO_ON_2, ON_2)
      insertTransferPayee(TRANSFER_TO_OFF, OFF)
      CategoryDao(this).insert(CategoryId(CATEGORY), "Category")
      insertDeletedCategory(DELETED_CATEGORY)
      action(TransactionDao(this))
    }

  private suspend fun BudgetDatabase.insertDeletedCategory(id: String) {
    categoriesQueries.withoutResult {
      insert(
        Categories(
          id = CategoryId(id),
          name = "Deleted",
          is_income = false,
          cat_group = null,
          sort_order = null,
          tombstone = true,
          hidden = false,
          goal_def = null,
          template_settings = null,
          cleanup_def = null,
        )
      )
    }
    categoryMappingQueries.withoutResult {
      insert(id = CategoryId(id), transferId = CategoryId(id))
    }
  }

  private suspend fun BudgetDatabase.insertTransferPayee(id: String, account: String) {
    payeesQueries.withoutResult {
      insert(
        id = PayeeId(id),
        name = null,
        category = null,
        tombstone = false,
        transfer_acct = AccountId(account),
        favorite = false,
        learn_categories = null,
      )
    }
    payeeMappingQueries.withoutResult { insert(id = PayeeId(id), targetId = PayeeId(id)) }
  }

  private companion object {
    val DATE = LocalDate(2026, 1, 1)
    const val ON_1 = "on1"
    const val ON_2 = "on2"
    const val OFF = "off"
    const val PAYEE = "payee"
    const val CATEGORY = "category"
    const val DELETED_CATEGORY = "deleted-category"
    const val TRANSFER_TO_ON_1 = "transfer-on1"
    const val TRANSFER_TO_ON_2 = "transfer-on2"
    const val TRANSFER_TO_OFF = "transfer-off"
  }
}
