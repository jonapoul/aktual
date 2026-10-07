package aktual.budget.banksync.domain

import aktual.api.model.banksync.BankSyncTransaction
import aktual.budget.db.dao.DatabaseTables.ACCOUNTS
import aktual.budget.db.dao.DatabaseTables.TRANSACTIONS
import aktual.budget.model.Amount
import aktual.budget.model.CategoryId
import aktual.budget.model.Condition
import aktual.budget.model.Field
import aktual.budget.model.MessageValue
import aktual.budget.model.Operator
import aktual.budget.model.RuleAction
import aktual.budget.model.SyncedPrefKey.PerAccount.CustomSyncMappings
import aktual.budget.model.SyncedPrefKey.PerAccount.SyncImportNotes
import aktual.budget.model.SyncedPrefKey.PerAccount.SyncImportPending
import aktual.budget.model.SyncedPrefKey.PerAccount.SyncImportTransactions
import aktual.budget.model.SyncedPrefKey.PerAccount.SyncReimportDeleted
import aktual.budget.model.SyncedPrefKey.PerAccount.SyncUpdateDates
import aktual.budget.model.TransactionId
import aktual.budget.transactions.domain.NewTransaction
import assertk.all
import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.containsExactly
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNull
import assertk.assertions.prop
import kotlin.test.Test
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.put

internal class BankSyncImporterTest {
  @Test
  fun `New transactions are added in the server's order`() = runBankSyncTest {
    val result =
      import(
        bankTx("-12.34", transactionId = "t1"),
        bankTx("5.00", date = "2026-09-29", payeeName = "Employer", transactionId = "t2"),
      )

    assertThat(result.updated).isEmpty()
    assertThat(result.added).hasSize(2)
    val (first, second) = result.added
    assertThat(liveIds()).containsExactly(first, second)
    assertThat(payeeNames()).containsExactly("Employer", "Tesco")

    val row = row(first)
    assertThat(row).all {
      prop("amount") { it.amount }.isEqualTo(Amount(-1234))
      prop("date") { it.date }.isEqualTo(LocalDate(2026, 9, 30))
      prop("financial_id") { it.financial_id }.isEqualTo("t1")
      prop("imported_description") { it.imported_description?.value }.isEqualTo("Tesco")
      prop("cleared") { it.cleared }.isEqualTo(true)
      prop("sort_order") { it.sort_order }.isEqualTo(NOW.toEpochMilliseconds().toDouble())
    }
    assertThat(row.description?.let { payeeDao.name(it) }).isEqualTo("Tesco")
    val payee = row.description?.value
    assertThat(row.raw_synced_data)
      .isEqualTo(
        """{"booked":true,"date":"2026-09-30","payeeName":"Tesco","transactionId":"t1",""" +
          """"transactionAmount":{"amount":"-12.34","currency":"GBP"},""" +
          """"account":"account-1","cleared":true,"amount":"-12.34",""" +
          """"imported_payee":"Tesco","payee":"$payee"}""",
      )
    assertThat(row(second).sort_order)
      .isEqualTo((NOW.toEpochMilliseconds() - TRANSACTION_SORT_INCREMENT).toDouble())
  }

  @Test
  fun `Rules run before saving, and payees nothing uses aren't created`() = runBankSyncTest {
    insertCategory(GROCERIES)
    insertRule(
      "tesco",
      conditions = listOf(cond(ImportedPayee, Contains, "tesco")),
      actions = listOf(set(PayeeName, "Tesco"), set(Category, GROCERIES.value)),
    )

    val result = import(bankTx("-1.00", payeeName = "TESCO STORES 1234", transactionId = "t1"))

    assertThat(payeeNames()).containsExactly("Tesco")
    val row = row(result.added.single())
    assertThat(row.category).isEqualTo(GROCERIES)
    assertThat(row.imported_description?.value).isEqualTo("TESCO STORES 1234")
    assertThat(row.description?.let { payeeDao.name(it) }).isEqualTo("Tesco")
  }

  @Test
  fun `A rule deleting the transaction stops it being added`() = runBankSyncTest {
    insertRule(
      "delete",
      conditions = listOf(cond(ImportedPayee, Contains, "fee")),
      actions = listOf(RuleAction(value = JsonPrimitive(""), op = DeleteTransaction)),
    )

    val result =
      import(
        bankTx("-1.00", payeeName = "Card fee", transactionId = "t1"),
        bankTx("-2.00", transactionId = "t2"),
      )

    assertThat(result.added).hasSize(1)
    assertThat(row(result.added.single()).financial_id).isEqualTo("t2")
    assertThat(payeeNames()).containsExactly("Tesco")
  }

  @Test
  fun `A match on imported_id only fills in what changed`() = runBankSyncTest {
    val id = import(bankTx("-12.34", transactionId = "t1")).added.single()

    val result = import(bankTx("-12.34", transactionId = "t1", notes = "Lunch"))

    assertThat(result.added).isEmpty()
    assertThat(result.updated).containsExactly(id)
    assertThat(lastSync()).containsExactly(Change(TRANSACTIONS, id.value, "notes", string("Lunch")))
  }

  @Test
  fun `Nothing is sent when a match has nothing new`() = runBankSyncTest {
    import(bankTx("-12.34", transactionId = "t1"))

    val result = import(bankTx("-12.34", transactionId = "t1"))

    assertThat(result.updated).isEmpty()
    assertThat(syncCalls).hasSize(1)
  }

  @Test
  fun `A booked transaction matches the pending one it replaces`() = runBankSyncTest {
    val pending = import(bankTx("-12.34", booked = false, internalTransactionId = "i1")).added
    assertThat(row(pending.single())).all {
      prop("cleared") { it.cleared }.isEqualTo(false)
      prop("financial_id") { it.financial_id }.isNull()
    }

    val result = import(bankTx("-12.34", date = "2026-10-01", transactionId = "t1"))

    assertThat(result.added).isEmpty()
    assertThat(result.updated).isEqualTo(pending)
    assertThat(row(pending.single())).all {
      prop("cleared") { it.cleared }.isEqualTo(true)
      prop("financial_id") { it.financial_id }.isEqualTo("t1")
      // Dates aren't updated by default
      prop("date") { it.date }.isEqualTo(LocalDate(2026, 9, 30))
    }
  }

  @Test
  fun `Pending transactions are skipped when not imported`() = runBankSyncTest {
    preferences[SyncImportPending(ACCOUNT)] = "false"

    val result = import(bankTx("-1.00", booked = false), bankTx("-2.00", transactionId = "t2"))

    assertThat(result.added.map { row(it).financial_id }).containsExactly("t2")
  }

  @Test
  fun `Without a transaction id, the internal one is used with the account`() = runBankSyncTest {
    val later = import(bankTx("-1.00", internalTransactionId = "i1")).added.single()
    val first =
      import(bankTx("-2.00", internalTransactionId = "i2"), initialSync = true).added.last()

    assertThat(row(later).financial_id).isEqualTo("account-1-i1")
    // On a first sync the provider's object has no account yet
    assertThat(row(first).financial_id).isEqualTo("undefined-i2")
  }

  @Test
  fun `Different imported_ids aren't merged without a sync source`() = runBankSyncTest {
    import(bankTx("-12.34", transactionId = "t1"))

    val result = import(bankTx("-12.34", transactionId = "t2"))

    assertThat(result.added).hasSize(1)
    assertThat(liveIds()).hasSize(2)
  }

  @Test
  fun `With a sync source, an unlinked transaction is preferred`() = runBankSyncTest {
    val linked = import(bankTx("-12.34", transactionId = "t1")).added.single()
    val payee = row(linked).description
    val unlinked = writer.write {
      insert(NewTransaction(account = ACCOUNT, date = DATE, amount = Amount(-1234), payee = payee))
    }

    val result = import(bankTx("-12.34", transactionId = "t2"), source = GoCardless)

    assertThat(result.added).isEmpty()
    assertThat(result.updated).containsExactly(unlinked)
    assertThat(row(unlinked).financial_id).isEqualTo("t2")
    assertThat(row(linked).financial_id).isEqualTo("t1")
  }

  @Test
  fun `Same payee is matched first`() = runBankSyncTest {
    val other = writer.write {
      insert(NewTransaction(account = ACCOUNT, date = DATE, amount = Amount(-500)))
    }
    val tesco = writer.write {
      val payee = insertPayee("Tesco")
      insert(
        NewTransaction(
          account = ACCOUNT,
          date = DATE.minus(2, DAY),
          amount = Amount(-500),
          payee = payee,
        ),
      )
    }

    val result = import(bankTx("-5.00", transactionId = "t1"))

    assertThat(result.updated).containsExactly(tesco)
    assertThat(row(other).financial_id).isNull()
  }

  @Test
  fun `Children of a split matched on imported_id aren't matched again`() = runBankSyncTest {
    val (parent, child) = insertSplit(importedId = "t1")

    val result =
      import(bankTx("-10.00", transactionId = "t1"), bankTx("-6.00", transactionId = "t2"))

    assertThat(result.updated).containsExactly(parent)
    assertThat(result.added).hasSize(1)
    assertThat(row(child).financial_id).isNull()
    assertThat(row(parent).financial_id).isEqualTo("t1")
  }

  @Test
  fun `Children of an unmatched split can be matched`() = runBankSyncTest {
    val (_, child) = insertSplit(importedId = null)

    val result = import(bankTx("-6.00", transactionId = "t2"))

    assertThat(result.updated).containsExactly(child)
    assertThat(row(child).financial_id).isEqualTo("t2")
  }

  @Test
  fun `Updated dates and cleared flags carry over to split children`() = runBankSyncTest {
    preferences[SyncUpdateDates(ACCOUNT)] = "true"
    val (parent, child) = insertSplit(importedId = "t1", cleared = false)

    val result = import(bankTx("-10.00", date = "2026-09-28", transactionId = "t1"))

    val date = LocalDate(2026, 9, 28)
    assertThat(result.updated).containsExactlyInAnyOrder(parent, child, CHILD_2)
    for (id in listOf(parent, child, CHILD_2)) {
      assertThat(row(id)).all {
        prop("date") { it.date }.isEqualTo(date)
        prop("cleared") { it.cleared }.isEqualTo(true)
      }
    }
  }

  @Test
  fun `Deleted transactions are imported again by default`() = runBankSyncTest {
    val id = import(bankTx("-1.00", transactionId = "t1")).added.single()
    writer.write { delete(id) }

    val result = import(bankTx("-1.00", transactionId = "t1"))

    assertThat(result.added).hasSize(1)
  }

  @Test
  fun `Deleted transactions stay deleted when not reimported`() = runBankSyncTest {
    preferences[SyncReimportDeleted(ACCOUNT)] = "false"
    val id = import(bankTx("-1.00", transactionId = "t1")).added.single()
    writer.write { delete(id) }

    val result = import(bankTx("-1.00", transactionId = "t1"))

    assertThat(result.added).isEmpty()
    assertThat(liveIds()).isEmpty()
  }

  @Test
  fun `Reconciled transactions are left alone`() = runBankSyncTest {
    val id = writer.write {
      insert(
        NewTransaction(
          account = ACCOUNT,
          date = DATE,
          amount = Amount(-100),
          importedId = "t1",
          reconciled = true,
        ),
      )
    }

    val result = import(bankTx("-1.00", transactionId = "t1", notes = "Lunch"))

    assertThat(result.updated).isEmpty()
    assertThat(row(id).notes).isNull()
  }

  @Test
  fun `Field mappings pick the date, payee and notes`() = runBankSyncTest {
    preferences[CustomSyncMappings(ACCOUNT)] =
      """{"payment":{"date":"bookingDate","payee":"creditorName","notes":"remittance"}}"""

    val result =
      import(
        bankTx("-1.00", transactionId = "t1") {
          put("bookingDate", "2026-09-25")
          put("creditorName", "Shop")
          put("remittance", " Ref #12 ")
        },
        bankTx("2.00", transactionId = "t2", notes = "Pay #1"),
      )

    val (payment, deposit) = result.added.map { row(it) }
    assertThat(payment).all {
      prop("date") { it.date }.isEqualTo(LocalDate(2026, 9, 25))
      prop("imported_description") { it.imported_description?.value }.isEqualTo("Shop")
      prop("notes") { it.notes }.isEqualTo("Ref ##12")
    }
    assertThat(deposit).all {
      prop("date") { it.date }.isEqualTo(DATE)
      prop("imported_description") { it.imported_description?.value }.isEqualTo("Tesco")
      prop("notes") { it.notes }.isEqualTo("Pay ##1")
    }
  }

  @Test
  fun `Notes aren't imported when turned off`() = runBankSyncTest {
    preferences[SyncImportNotes(ACCOUNT)] = "false"

    val result = import(bankTx("-1.00", transactionId = "t1", notes = "Lunch"))

    assertThat(row(result.added.single()).notes).isNull()
  }

  @Test
  fun `Only live categories from the provider are kept`() = runBankSyncTest {
    insertCategory(GROCERIES)

    val result =
      import(
        bankTx("-1.00", transactionId = "t1") { put("category", GROCERIES.value) },
        bankTx("-2.00", transactionId = "t2") { put("category", "missing") },
      )

    assertThat(result.added.map { row(it).category }).containsExactly(GROCERIES, null)
  }

  @Test
  fun `The first sync adds a starting balance`() = runBankSyncTest {
    val download =
      BankSyncDownload(
        transactions =
          listOf(
              bankTx("-12.34", transactionId = "t1"),
              bankTx("5.00", date = "2026-09-28", transactionId = "t2"),
            )
            .map(::BankSyncTransaction),
        currentBalance = Amount(10_000),
      )

    val result = importer.import(ACCOUNT, SimpleFin, download, initialSync = true)

    assertThat(result.added).hasSize(3)
    assertThat(row(result.added.first())).all {
      prop("starting_balance_flag") { it.starting_balance_flag }.isEqualTo(true)
      prop("amount") { it.amount }.isEqualTo(Amount(10_734))
      prop("date") { it.date }.isEqualTo(LocalDate(2026, 9, 28))
    }
    // The account's balance is only updated on later syncs
    assertThat(messages().filter { it.dataset == ACCOUNTS }).isEmpty()
  }

  @Test
  fun `Later syncs update the account's balance`() = runBankSyncTest {
    val download =
      BankSyncDownload(
        listOf(BankSyncTransaction(bankTx("-1.00", transactionId = "t1"))),
        currentBalance = Amount(500),
      )

    importer.import(ACCOUNT, GoCardless, download)

    assertThat(lastSync())
      .contains(Change(ACCOUNTS, ACCOUNT.value, "balance_current", MessageValue.Number(500)))
  }

  @Test
  fun `Only the balance is updated when transactions aren't imported`() = runBankSyncTest {
    preferences[SyncImportTransactions(ACCOUNT)] = "false"
    val download =
      BankSyncDownload(
        listOf(BankSyncTransaction(bankTx("-1.00", transactionId = "t1"))),
        currentBalance = Amount(500),
      )

    val result = importer.import(ACCOUNT, GoCardless, download)

    assertThat(result.added).isEmpty()
    assertThat(lastSync())
      .containsExactly(Change(ACCOUNTS, ACCOUNT.value, "balance_current", MessageValue.Number(500)))
  }

  @Test
  fun `Invalid amounts fail`() = runBankSyncTest {
    assertFailure { import(bankTx("abc", transactionId = "t1")) }
      .isInstanceOf<IllegalArgumentException>()
    assertThat(syncCalls).isEmpty()
  }

  // A split of -10.00 into -6.00 and -4.00 on DATE
  private suspend fun BankSyncTestScope.insertSplit(
    importedId: String?,
    cleared: Boolean = true,
  ): Pair<TransactionId, TransactionId> = writer.write {
    val parent =
      insert(
        NewTransaction(
          id = PARENT,
          account = ACCOUNT,
          date = DATE,
          amount = Amount(-1000),
          importedId = importedId,
          cleared = cleared,
          isParent = true,
        ),
      )
    val child =
      insert(
        NewTransaction(
          id = CHILD_1,
          account = ACCOUNT,
          date = DATE,
          amount = Amount(-600),
          cleared = cleared,
          parentId = parent,
        ),
      )
    insert(
      NewTransaction(
        id = CHILD_2,
        account = ACCOUNT,
        date = DATE,
        amount = Amount(-400),
        cleared = cleared,
        parentId = parent,
      ),
    )
    parent to child
  }

  private fun cond(field: Field, op: Operator, value: String) =
    Condition(field = field, operator = op, value = JsonPrimitive(value))

  private fun set(field: Field, value: String) =
    RuleAction(value = JsonPrimitive(value), op = RuleAction.Op.Set, field = field)

  private companion object {
    val DATE = LocalDate(2026, 9, 30)
    val GROCERIES = CategoryId("groceries")
    val PARENT = TransactionId("parent")
    val CHILD_1 = TransactionId("child-1")
    val CHILD_2 = TransactionId("child-2")
    const val TRANSACTION_SORT_INCREMENT = 1024L
  }
}
