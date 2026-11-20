package aktual.budget.transactions.domain

import aktual.budget.db.Transactions
import aktual.budget.db.dao.DatabaseTables.TRANSACTIONS
import aktual.budget.db.dao.RepairTransactionsDao
import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.CategoryId
import aktual.budget.model.TransactionId
import aktual.budget.model.localChange
import assertk.all
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.prop
import kotlin.test.Test
import kotlinx.datetime.LocalDate

internal class TransactionRepairerTest {
  @Test
  fun `Nothing to repair`() = runWriterTest {
    insertAccount(ACCOUNT)
    insertSplit(amount = -100, parts = listOf(-60, -40))

    val result = repairer().repair()

    assertThat(result).isEqualTo(RepairResult(0, 0, 0, 0, 0, 0, mismatchedSplits = emptyList()))
  }

  @Test
  fun `Split parts get the parent's payee and cleared flag`() = runWriterTest {
    insertAccount(ACCOUNT)
    val payee = writer.write { insertPayee("Tesco") }
    val (parent, parts) = insertSplit(amount = -100, parts = listOf(-60, -40))
    writer.write {
      update(TransactionUpdate(parent, payee = Patch.To(payee), cleared = false))
      update(TransactionUpdate(parts[1], payee = Patch.To(payee), cleared = false))
    }

    val result = repairer().repair()

    assertThat(result).all {
      prop(RepairResult::blankPayees).isEqualTo(1)
      prop(RepairResult::cleared).isEqualTo(1)
    }
    assertThat(transactionDao.row(parts[0])).isNotNull().all {
      prop(Transactions::description).isEqualTo(payee)
      prop(Transactions::cleared).isEqualTo(false)
    }
  }

  @Test
  fun `Parts of a deleted parent are deleted`() = runWriterTest {
    insertAccount(ACCOUNT)
    val (parent, parts) = insertSplit(amount = -100, parts = listOf(-60, -40))
    invoke(listOf(localChange(TRANSACTIONS, parent.value, "tombstone", 1L)))

    val result = repairer().repair()

    assertThat(result.deleted).isEqualTo(2)
    parts.forEach { part ->
      assertThat(transactionDao.row(part)).isNotNull().prop(Transactions::tombstone).isEqualTo(true)
    }
  }

  @Test
  fun `A deleted parent's transfer part keeps its other side`() = runWriterTest {
    insertAccount(ACCOUNT)
    insertAccount(OTHER)
    val (parent, parts) = insertSplit(amount = -100, parts = listOf(-100))
    val other = writer.write {
      insert(NewTransaction(OTHER, DATE, Amount(100), transferId = parts[0]))
    }
    invoke(
      listOf(
        localChange(TRANSACTIONS, parts[0].value, "transferred_id", other.value),
        localChange(TRANSACTIONS, parent.value, "tombstone", 1L),
      ),
    )

    repairer().repair()

    assertThat(transactionDao.row(parts[0]))
      .isNotNull()
      .prop(Transactions::tombstone)
      .isEqualTo(true)
    assertThat(transactionDao.row(other)).isNotNull().prop(Transactions::tombstone).isEqualTo(false)
  }

  @Test
  fun `Splits that don't add up are reported`() = runWriterTest {
    insertAccount(ACCOUNT)
    insertSplit(amount = -100, parts = listOf(-60, -30))

    val result = repairer().repair()

    assertThat(result.mismatchedSplits).containsExactly(DATE)
  }

  @Test
  fun `Transfers between on-budget accounts lose their category`() = runWriterTest {
    insertAccount(ACCOUNT)
    insertAccount(OTHER)
    insertCategory(GROCERIES, name = "Groceries")
    val from = writer.write { insert(NewTransaction(ACCOUNT, DATE, Amount(-100))) }
    val to = writer.write { insert(NewTransaction(OTHER, DATE, Amount(100), transferId = from)) }
    invoke(
      listOf(
        localChange(TRANSACTIONS, from.value, "transferred_id", to.value),
        localChange(TRANSACTIONS, from.value, "category", GROCERIES.value),
      ),
    )

    val result = repairer().repair()

    assertThat(result.transfers).isEqualTo(1)
    assertThat(transactionDao.row(from)).isNotNull().prop(Transactions::category).isNull()
  }

  @Test
  fun `Errors are cleared from non-parents and categories from parents`() = runWriterTest {
    insertAccount(ACCOUNT)
    insertCategory(GROCERIES, name = "Groceries")
    val (parent, _) = insertSplit(amount = -100, parts = listOf(-60, -40))
    val plain = writer.write { insert(NewTransaction(ACCOUNT, DATE, Amount(-5))) }
    invoke(
      listOf(
        localChange(TRANSACTIONS, plain.value, "error", """{"type":"SplitTransactionError"}"""),
        localChange(TRANSACTIONS, parent.value, "category", GROCERIES.value),
      ),
    )

    val result = repairer().repair()

    assertThat(result).all {
      prop(RepairResult::nonParentErrors).isEqualTo(1)
      prop(RepairResult::categorisedParents).isEqualTo(1)
    }
    assertThat(transactionDao.row(plain)).isNotNull().prop(Transactions::error).isNull()
    assertThat(transactionDao.row(parent)).isNotNull().prop(Transactions::category).isNull()
  }

  private fun WriterTestScope.repairer() =
    TransactionRepairerImpl(RepairTransactionsDao(database), writer)

  private suspend fun WriterTestScope.insertSplit(
    amount: Long,
    parts: List<Long>,
  ): Pair<TransactionId, List<TransactionId>> = writer.write {
    val parent = insert(NewTransaction(ACCOUNT, DATE, Amount(amount), isParent = true))
    val children = parts.map { part ->
      insert(NewTransaction(ACCOUNT, DATE, Amount(part), parentId = parent))
    }
    parent to children
  }

  private companion object {
    val ACCOUNT = AccountId("account")
    val OTHER = AccountId("other")
    val GROCERIES = CategoryId("groceries")
    val DATE = LocalDate(2026, 10, 1)
  }
}
