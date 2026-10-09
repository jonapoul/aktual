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
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

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
  fun `A category's transactions in a month include split parts and skip off budget accounts`() =
    runDaoTest { transactions ->
      // given
      transactions.insert("t1", ON_1, CATEGORY, PAYEE, LocalDate(2026, 1, 5))
      transactions.insert("t2", ON_2, CATEGORY, PAYEE, LocalDate(2026, 1, 20))
      transactions.insert("t3", ON_1, CATEGORY, PAYEE, LocalDate(2026, 2, 1))
      transactions.insert("t4", OFF, CATEGORY, PAYEE, LocalDate(2026, 1, 6))
      transactions.insert("t5", ON_1, category = null, PAYEE, LocalDate(2026, 1, 7))
      transactions.insert("p", ON_1, category = null, PAYEE, LocalDate(2026, 1, 8), isParent = true)
      transactions.insert("c1", ON_1, CATEGORY, PAYEE, LocalDate(2026, 1, 8), parent = "p")
      val january = LocalDate(2026, 1, 1)..LocalDate(2026, 1, 31)

      // when
      val all = transactions.getByCategoryPaged(CategoryId(CATEGORY), january, null, 10, 0)
      val account =
        transactions.getByCategoryPaged(CategoryId(CATEGORY), january, AccountId(ON_1), 10, 0)
      val ids = transactions.getIdsAndNotesByCategory(CategoryId(CATEGORY), january, null)

      // then
      assertThat(all.map { it.id.toString() }).containsExactly("t2", "c1", "t1")
      assertThat(account.map { it.id.toString() }).containsExactly("c1", "t1")
      assertThat(ids.map { it.id.toString() }).containsExactly("t2", "c1", "t1")
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
  fun `Rows are off budget by their account and transfers by their payee`() =
    runDaoTest { transactions ->
      // given
      transactions.insert("t1", ON_1, CATEGORY, PAYEE, DATE)
      transactions.insert("t2", ON_1, category = null, TRANSFER_TO_ON_2, DATE)
      transactions.insert("t3", ON_1, category = null, TRANSFER_TO_OFF, DATE)
      transactions.insert("t4", OFF, category = null, PAYEE, DATE)
      transactions.insert("t5", OFF, category = null, TRANSFER_TO_ON_1, DATE)

      // when
      val rows = transactions.getPaged(limit = 10, offset = 0).rows

      // then
      assertThat(rows.associate { it.id.toString() to it.offBudget })
        .isEqualTo(mapOf("t1" to false, "t2" to false, "t3" to false, "t4" to true, "t5" to true))
      assertThat(rows.associate { it.id.toString() to it.isTransfer })
        .isEqualTo(mapOf("t1" to false, "t2" to true, "t3" to false, "t4" to false, "t5" to true))
    }

  @Test
  fun `Transfers name the account on the other side`() = runDaoTest { transactions ->
    // given
    transactions.insert("t1", ON_1, CATEGORY, PAYEE, DATE)
    transactions.insert("t2", ON_1, category = null, TRANSFER_TO_ON_2, DATE)
    transactions.insert("t3", ON_1, category = null, TRANSFER_TO_OFF, DATE)

    // when
    val rows = transactions.getPaged(limit = 10, offset = 0).rows

    // then
    assertThat(rows.associate { it.id.toString() to it.transferAccountName })
      .isEqualTo(mapOf("t1" to null, "t2" to "Savings", "t3" to "Mortgage"))
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

  @Test
  fun `Paged lists skip split children and keep their parents`() = runDaoTest { transactions ->
    // given
    transactions.insert("t1", ON_1, CATEGORY, PAYEE, DATE)
    transactions.insert("p", ON_1, category = null, PAYEE, DATE, isParent = true)
    transactions.insert("c1", ON_1, CATEGORY, PAYEE, DATE, parent = "p")
    transactions.insert("c2", ON_1, CATEGORY, PAYEE, DATE, parent = "p")
    transactions.insert("t2", ON_2, CATEGORY, PAYEE, DATE)

    // when
    val all = transactions.getPaged(limit = 10, offset = 0).rows
    val account = transactions.getByAccountPaged(AccountId(ON_1), limit = 10, offset = 0).rows

    // then
    assertThat(all.associate { it.id.toString() to it.isParent })
      .isEqualTo(mapOf("p" to true, "t1" to false, "t2" to false))
    assertThat(account.map { it.id.toString() }).containsExactly("p", "t1")
  }

  @Test
  fun `A split above the offset doesn't shift the balance`() = runDaoTest { transactions ->
    // given
    val newest = LocalDate(2026, 1, 2)
    transactions.insert("t1", ON_1, CATEGORY, PAYEE, DATE, amount = 10.0)
    transactions.insert("t2", ON_1, CATEGORY, PAYEE, DATE, amount = 20.0)
    transactions.insert("p", ON_1, category = null, PAYEE, newest, amount = 100.0, isParent = true)
    transactions.insert("c1", ON_1, CATEGORY, PAYEE, newest, amount = 60.0, parent = "p")
    transactions.insert("c2", ON_1, CATEGORY, PAYEE, newest, amount = 40.0, parent = "p")

    // when
    val all = transactions.getPaged(limit = 1, offset = 1)
    val account = transactions.getByAccountPaged(AccountId(ON_1), limit = 1, offset = 1)

    // then
    assertThat(all.rows.map { it.id.toString() }).containsExactly("t1")
    assertThat(all.topBalance).isEqualTo(3000L)
    assertThat(account.rows.map { it.id.toString() }).containsExactly("t1")
    assertThat(account.topBalance).isEqualTo(3000L)
  }

  @Test
  fun `Children of a split are live and in entry order`() = runDaoTest { transactions ->
    // given
    transactions.insert("p", ON_1, category = null, PAYEE, DATE, isParent = true)
    transactions.insert("c1", ON_1, CATEGORY, PAYEE, DATE, parent = "p")
    transactions.insert("c2", ON_1, CATEGORY, PAYEE, DATE, parent = "p")
    transactions.insert("c3", ON_1, category = null, PAYEE, DATE, parent = "p")
    insertCopy(transactions, id = "c4", copyOf = "c1") { it.copy(tombstone = true) }
    insertCopy(transactions, id = "c0", copyOf = "c1") { it.copy(sort_order = MAX_VALUE) }
    insertCopy(transactions, id = "dead", copyOf = "p") { it.copy(tombstone = true) }
    transactions.insert("d1", ON_1, CATEGORY, PAYEE, DATE, parent = "dead")
    transactions.insert("other", ON_1, category = null, PAYEE, DATE, isParent = true)
    transactions.insert("o1", ON_1, CATEGORY, PAYEE, DATE, parent = "other")

    // when
    val children = transactions.childrenOf(listOf(TransactionId("p"), TransactionId("dead")))

    // then
    assertThat(children.mapValues { (_, rows) -> rows.map { it.id.toString() } })
      .isEqualTo(mapOf(TransactionId("p") to listOf("c0", "c1", "c2", "c3")))
    assertThat(children.getValue(TransactionId("p")).map { it.needsCategory })
      .containsExactly(false, false, false, true)
  }

  @Test
  fun `Only an unbalanced split parent has a split difference`() = runDaoTest { transactions ->
    // given
    val unbalanced = splitError(difference = 500)
    val unknown = buildJsonObject { put("type", "SomethingElse") }
    transactions.insert(
      "p",
      ON_1,
      category = null,
      PAYEE,
      DATE,
      isParent = true,
      error = unbalanced,
    )
    transactions.insert("c", ON_1, CATEGORY, PAYEE, DATE, parent = "p", error = unbalanced)
    transactions.insert("q", ON_1, category = null, PAYEE, DATE, isParent = true)
    transactions.insert("r", ON_1, category = null, PAYEE, DATE, isParent = true, error = unknown)
    transactions.insert(
      id = "s",
      account = ON_1,
      category = null,
      payee = PAYEE,
      date = DATE,
      isParent = true,
      error = splitError(difference = 0),
    )

    // when
    val rows = transactions.getPaged(limit = 10, offset = 0).rows
    val children = transactions.childrenOf(listOf(TransactionId("p"))).getValue(TransactionId("p"))

    // then
    assertThat(rows.associate { it.id.toString() to it.splitDifference })
      .isEqualTo(mapOf("p" to 500L, "q" to null, "r" to null, "s" to null))
    assertThat(children.map { it.splitDifference }).containsExactly(null)
  }

  private fun splitError(difference: Long) = buildJsonObject {
    put("type", "SplitTransactionError")
    put("version", 1)
    put("difference", difference)
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
        buildAccount(id = AccountId(ON_2), name = "Savings"),
        buildAccount(id = AccountId(OFF), name = "Mortgage", offBudget = true),
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
        ),
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
