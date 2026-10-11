package aktual.budget.transactions.domain

import aktual.budget.db.Transactions
import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.CategoryId
import aktual.budget.model.PayeeId
import aktual.budget.model.TransactionId
import assertk.all
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNotEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isTrue
import assertk.assertions.prop
import kotlin.test.Test
import kotlinx.datetime.LocalDate

// The first three are packages/loot-core/src/server/transactions/transfer.test.ts
internal class TransfersTest {
  @Test
  fun `Transfers are properly inserted, updated and deleted`() = runWriterTest {
    val payees = prepareDatabase()
    val ordinary = writer.write { insertPayee("Non-transfer") }
    insert(NewTransaction(account = ONE, date = DATE, amount = Amount(5000), payee = ordinary))
    assertThat(syncCalls).hasSize(5)

    val id =
      insert(NewTransaction(account = ONE, date = DATE, amount = Amount(5000), payee = payees.two))
    val other = otherSide(id)
    assertThat(row(other)).all {
      prop("acct") { it.acct }.isEqualTo(TWO)
      prop("amount") { it.amount }.isEqualTo(Amount(-5000))
      prop("description") { it.description }.isEqualTo(payees.one)
      prop("date") { it.date }.isEqualTo(DATE)
      prop("notes") { it.notes }.isNull()
      prop("category") { it.category }.isNull()
      prop("cleared") { it.cleared }.isEqualTo(false)
      prop("transferred_id") { it.transferred_id }.isEqualTo(id)
    }

    // The other side takes the notes, and keeps its date
    update(TransactionUpdate(id, date = LocalDate(2017, 1, 5), notes = Patch.To("This is a note")))
    assertThat(row(other)).all {
      prop("notes") { it.notes }.isEqualTo("This is a note")
      prop("date") { it.date }.isEqualTo(DATE)
      prop("amount") { it.amount }.isEqualTo(Amount(-5000))
    }

    update(TransactionUpdate(id, payee = Patch.To(payees.three)))
    assertThat(otherSide(id)).isEqualTo(other)
    assertThat(row(other)).all {
      prop("acct") { it.acct }.isEqualTo(THREE)
      prop("description") { it.description }.isEqualTo(payees.one)
    }

    val notTransferred = writer.write { insertPayee("Not transferred anymore") }
    update(TransactionUpdate(id, payee = Patch.To(notTransferred)))
    assertThat(row(id).transferred_id).isNull()
    assertThat(row(other).tombstone).isEqualTo(true)

    // Re-transfer it
    update(TransactionUpdate(id, payee = Patch.To(payees.two)))
    val second = otherSide(id)
    assertThat(second).isNotEqualTo(other)
    assertThat(row(second)).all {
      prop("acct") { it.acct }.isEqualTo(TWO)
      prop("notes") { it.notes }.isEqualTo("This is a note")
      prop("date") { it.date }.isEqualTo(LocalDate(2017, 1, 5))
      prop("tombstone") { it.tombstone }.isEqualTo(false)
    }

    writer.write(runTransfers = true) { delete(id) }
    assertThat(row(id).tombstone).isEqualTo(true)
    assertThat(row(second).tombstone).isEqualTo(true)
  }

  @Test
  fun `Transfers are properly de-categorized`() = runWriterTest {
    val payees = prepareDatabase()
    val ordinary = writer.write { insertPayee("Non-transfer") }
    val transaction =
      NewTransaction(
        account = ONE,
        date = DATE,
        amount = Amount(5000),
        payee = ordinary,
        category = CATEGORY,
      )
    val id = insert(transaction)

    // Money leaving the budget keeps its category
    update(TransactionUpdate(id, payee = Patch.To(payees.three), notes = Patch.To("hi")))
    assertThat(row(id).category).isEqualTo(CATEGORY)
    assertThat(row(otherSide(id)).category).isNull()

    update(TransactionUpdate(id, payee = Patch.To(payees.two)))
    assertThat(row(id).category).isNull()
    assertThat(row(otherSide(id))).all {
      prop("acct") { it.acct }.isEqualTo(TWO)
      prop("category") { it.category }.isNull()
    }
  }

  @Test
  fun `Split transfers are retained on child transactions`() = runWriterTest {
    val payees = prepareDatabase()
    val parent =
      insert(NewTransaction(account = ONE, date = DATE, amount = Amount(5000), payee = payees.two))
    val parentOther = otherSide(parent)

    // A split parent drops its transfer
    update(TransactionUpdate(parent, isParent = true))
    assertThat(row(parent).transferred_id).isNull()
    assertThat(row(parentOther).tombstone).isEqualTo(true)

    val part =
      NewTransaction(
        account = ONE,
        date = DATE,
        amount = Amount(2000),
        payee = payees.one,
        parentId = parent,
      )
    val child = insert(part)
    val childOther = otherSide(child)
    assertThat(childOther).isNotEqualTo(parentOther)
    assertThat(row(child).description).isEqualTo(payees.one)
    assertThat(row(childOther)).all {
      prop("acct") { it.acct }.isEqualTo(ONE)
      prop("amount") { it.amount }.isEqualTo(Amount(-2000))
    }
    assertThat(row(parent).transferred_id).isNull()
  }

  @Test
  fun `A new split parent with a transfer payee makes no transfer`() = runWriterTest {
    val payees = prepareDatabase()
    val before = syncCalls.size

    val parent =
      insert(NewTransaction(account = ONE, date = DATE, payee = payees.two, isParent = true))

    assertThat(row(parent).transferred_id).isNull()
    assertThat(syncCalls).hasSize(before + 1)
  }

  @Test
  fun `Transfers aren't run unless asked for`() = runWriterTest {
    val payees = prepareDatabase()
    val before = syncCalls.size

    val id = writer.write {
      insert(NewTransaction(account = ONE, date = DATE, amount = Amount(5000), payee = payees.two))
    }

    assertThat(row(id).transferred_id).isNull()
    assertThat(syncCalls).hasSize(before + 1)
  }

  @Test
  fun `The other side follows the amount and the account`() = runWriterTest {
    val payees = prepareDatabase()
    val id =
      insert(NewTransaction(account = ONE, date = DATE, amount = Amount(5000), payee = payees.two))
    val other = otherSide(id)

    update(TransactionUpdate(id, amount = Amount(-1234)))
    assertThat(row(other).amount).isEqualTo(Amount(1234))

    // Its payee is the account this side is in
    update(TransactionUpdate(id, account = THREE))
    assertThat(row(other)).all {
      prop("acct") { it.acct }.isEqualTo(TWO)
      prop("description") { it.description }.isEqualTo(payees.three)
    }
  }

  @Test
  fun `Nothing is written to the other side when it already matches`() = runWriterTest {
    val payees = prepareDatabase()
    val id =
      insert(NewTransaction(account = ONE, date = DATE, amount = Amount(5000), payee = payees.two))
    val before = syncCalls.size

    update(TransactionUpdate(id, cleared = false, date = LocalDate(2017, 2, 1)))

    assertThat(syncCalls).hasSize(before + 1)
  }

  @Test
  fun `Deleting the other side of a split part leaves the part in its split`() = runWriterTest {
    val payees = prepareDatabase()
    val parent = insert(NewTransaction(account = ONE, date = DATE, isParent = true))
    val part =
      NewTransaction(
        account = ONE,
        date = DATE,
        amount = Amount(2000),
        payee = payees.two,
        parentId = parent,
      )
    val child = insert(part)
    val other = otherSide(child)

    writer.write(runTransfers = true) { delete(other) }

    assertThat(row(other).tombstone).isEqualTo(true)
    assertThat(row(child)).all {
      prop("tombstone") { it.tombstone }.isEqualTo(false)
      prop("transferred_id") { it.transferred_id }.isNull()
      prop("description") { it.description }.isNull()
      prop("parent_id") { it.parent_id }.isEqualTo(parent)
    }
  }

  @Test
  fun `Deleting a split deletes the other side of its parts`() = runWriterTest {
    val payees = prepareDatabase()
    val parent = insert(NewTransaction(account = ONE, date = DATE, isParent = true))
    val child =
      insert(NewTransaction(account = ONE, date = DATE, payee = payees.two, parentId = parent))
    val other = otherSide(child)

    writer.write(runTransfers = true) { delete(parent) }

    assertThat(row(child).tombstone).isEqualTo(true)
    assertThat(row(other).tombstone).isEqualTo(true)
  }

  @Test
  fun `Both sides can be deleted together`() = runWriterTest {
    val payees = prepareDatabase()
    val id =
      insert(NewTransaction(account = ONE, date = DATE, amount = Amount(5000), payee = payees.two))
    val other = otherSide(id)

    writer.write(runTransfers = true) { delete(listOf(id, other)) }

    assertThat(row(id).tombstone).isEqualTo(true)
    assertThat(row(other).tombstone).isEqualTo(true)
  }

  @Test
  fun `A transfer between off budget accounts has no category`() = runWriterTest {
    val payees = prepareDatabase()
    insertAccount(FOUR, offBudget = true)
    val toFour = writer.write { insertPayee(name = "", transferAccount = FOUR) }

    val id = insert(NewTransaction(account = THREE, date = DATE, payee = toFour))

    assertThat(row(otherSide(id))).all {
      prop("acct") { it.acct }.isEqualTo(FOUR)
      prop("description") { it.description }.isEqualTo(payees.three)
      prop("category") { it.category }.isNull()
    }
  }

  @Test
  fun `A reconciled other side is reported when loading`() = runWriterTest {
    val payees = prepareDatabase()
    val loader = TransactionLoader(transactionDao)
    val id =
      insert(NewTransaction(account = ONE, date = DATE, amount = Amount(5000), payee = payees.two))
    assertThat(loader.load(id)).isNotNull().prop("flag") { it.hasReconciledTransfer }.isFalse()

    val other = otherSide(id)
    writer.write { update(TransactionUpdate(other, reconciled = true)) }

    assertThat(loader.load(id)).isNotNull().prop("flag") { it.hasReconciledTransfer }.isTrue()
  }

  @Test
  fun `A reconciled other side of a split's part is reported on the split`() = runWriterTest {
    val payees = prepareDatabase()
    val loader = TransactionLoader(transactionDao)
    val parent = insert(NewTransaction(account = ONE, date = DATE, isParent = true))
    val child =
      insert(NewTransaction(account = ONE, date = DATE, payee = payees.two, parentId = parent))
    writer.write { update(TransactionUpdate(otherSide(child), reconciled = true)) }

    assertThat(loader.load(parent)).isNotNull().prop("flag") { it.hasReconciledTransfer }.isTrue()
  }

  private data class TransferPayees(val one: PayeeId, val two: PayeeId, val three: PayeeId)

  // Two on budget accounts and an off budget one, each with its transfer payee
  private suspend fun WriterTestScope.prepareDatabase(): TransferPayees {
    insertCategory(CATEGORY, name = "cat1")
    insertAccount(ONE)
    insertAccount(TWO)
    insertAccount(THREE, offBudget = true)
    return TransferPayees(
      one = writer.write { insertPayee(name = "", transferAccount = ONE) },
      two = writer.write { insertPayee(name = "", transferAccount = TWO) },
      three = writer.write { insertPayee(name = "", transferAccount = THREE) },
    )
  }

  private suspend fun WriterTestScope.insert(transaction: NewTransaction): TransactionId =
    writer.write(runTransfers = true) { insert(transaction) }

  private suspend fun WriterTestScope.update(update: TransactionUpdate) =
    writer.write(runTransfers = true) { update(update) }

  private suspend fun WriterTestScope.row(id: TransactionId): Transactions =
    checkNotNull(transactionDao.row(id)) { "No transaction $id" }

  private suspend fun WriterTestScope.otherSide(id: TransactionId): TransactionId =
    checkNotNull(row(id).transferred_id) { "$id isn't a transfer" }

  private companion object {
    val ONE = AccountId("one")
    val TWO = AccountId("two")
    val THREE = AccountId("three")
    val FOUR = AccountId("four")
    val CATEGORY = CategoryId("1")
    val DATE = LocalDate(2017, 1, 1)
  }
}
