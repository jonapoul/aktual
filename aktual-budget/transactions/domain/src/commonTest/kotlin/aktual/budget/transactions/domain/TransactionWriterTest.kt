package aktual.budget.transactions.domain

import aktual.budget.db.dao.DatabaseTables.ACCOUNTS
import aktual.budget.db.dao.DatabaseTables.PAYEES
import aktual.budget.db.dao.DatabaseTables.PAYEE_MAPPING
import aktual.budget.db.dao.DatabaseTables.TRANSACTIONS
import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.BankSyncStatus
import aktual.budget.model.CategoryId
import aktual.budget.model.PayeeId
import aktual.budget.model.TransactionId
import app.cash.sqldelight.async.coroutines.awaitAsList
import assertk.all
import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.containsOnly
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isTrue
import assertk.assertions.prop
import kotlin.test.Test
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.yield
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

internal class TransactionWriterTest {
  @Test
  fun `Insert a transaction`() = runWriterTest {
    insertAccount(ACCOUNT)
    insertCategory(GROCERIES, name = "Groceries")
    val payee = writer.write { insertPayee("Tesco") }

    val id = writer.write {
      insert(
        NewTransaction(
          account = ACCOUNT,
          date = DATE,
          amount = Amount(-1234),
          payee = payee,
          category = GROCERIES,
          notes = "Weekly shop",
          importedId = "bank-123",
          importedPayee = "TESCO STORES 1234",
          cleared = false,
          rawSyncedData = """{"id":"bank-123"}""",
        )
      )
    }

    val row = transactionDao.row(id)
    assertThat(row).isNotNull().all {
      prop("acct") { it.acct }.isEqualTo(ACCOUNT)
      prop("category") { it.category }.isEqualTo(GROCERIES)
      prop("amount") { it.amount }.isEqualTo(Amount(-1234))
      prop("description") { it.description }.isEqualTo(payee)
      prop("notes") { it.notes }.isEqualTo("Weekly shop")
      prop("date") { it.date }.isEqualTo(DATE)
      prop("financial_id") { it.financial_id }.isEqualTo("bank-123")
      prop("imported_description") { it.imported_description?.value }.isEqualTo("TESCO STORES 1234")
      prop("sort_order") { it.sort_order }.isEqualTo(NOW.toEpochMilliseconds().toDouble())
      prop("cleared") { it.cleared }.isEqualTo(false)
      prop("reconciled") { it.reconciled }.isEqualTo(false)
      prop("isParent") { it.isParent }.isEqualTo(false)
      prop("isChild") { it.isChild }.isEqualTo(false)
      prop("starting_balance_flag") { it.starting_balance_flag }.isEqualTo(false)
      prop("tombstone") { it.tombstone }.isEqualTo(false)
      prop("raw_synced_data") { it.raw_synced_data }.isEqualTo("""{"id":"bank-123"}""")
    }

    // The view resolves the payee through payee_mapping
    val view = database.transactionsQueries.getByIds(listOf(id)).awaitAsList().single()
    assertThat(view.accountName).isEqualTo(ACCOUNT.value)
    assertThat(view.payeeName).isEqualTo("Tesco")
    assertThat(view.categoryName).isEqualTo("Groceries")

    // Null fields aren't sent
    val row1 = id.value
    assertThat(lastSync())
      .containsExactly(
        Change(TRANSACTIONS, row1, "acct", string(ACCOUNT.value)),
        Change(TRANSACTIONS, row1, "category", string(GROCERIES.value)),
        Change(TRANSACTIONS, row1, "amount", number(-1234)),
        Change(TRANSACTIONS, row1, "description", string(payee.value)),
        Change(TRANSACTIONS, row1, "notes", string("Weekly shop")),
        Change(TRANSACTIONS, row1, "date", number(20260915)),
        Change(TRANSACTIONS, row1, "financial_id", string("bank-123")),
        Change(TRANSACTIONS, row1, "imported_description", string("TESCO STORES 1234")),
        Change(TRANSACTIONS, row1, "sort_order", number(NOW.toEpochMilliseconds())),
        Change(TRANSACTIONS, row1, "cleared", number(0)),
        Change(TRANSACTIONS, row1, "reconciled", number(0)),
        Change(TRANSACTIONS, row1, "raw_synced_data", string("""{"id":"bank-123"}""")),
      )
  }

  @Test
  fun `Insert a split transaction`() = runWriterTest {
    insertAccount(ACCOUNT)
    insertCategory(GROCERIES, name = "Groceries")
    insertCategory(HOUSEHOLD, name = "Household")

    val (parent, children) =
      writer.write {
        val parent =
          insert(
            NewTransaction(
              account = ACCOUNT,
              date = DATE,
              amount = Amount(-3000),
              category = GROCERIES,
              isParent = true,
            )
          )
        val child1 =
          insert(
            NewTransaction(
              account = ACCOUNT,
              date = DATE,
              amount = Amount(-1000),
              category = GROCERIES,
              parentId = parent,
            )
          )
        val child2 =
          insert(
            NewTransaction(
              account = ACCOUNT,
              date = DATE,
              amount = Amount(-2000),
              category = HOUSEHOLD,
              parentId = parent,
            )
          )
        parent to listOf(child1, child2)
      }

    // Parents never have a category
    assertThat(transactionDao.row(parent)).isNotNull().all {
      prop("isParent") { it.isParent }.isEqualTo(true)
      prop("isChild") { it.isChild }.isEqualTo(false)
      prop("category") { it.category }.isNull()
      prop("parent_id") { it.parent_id }.isNull()
    }
    for (child in children) {
      assertThat(transactionDao.row(child)).isNotNull().all {
        prop("isParent") { it.isParent }.isEqualTo(false)
        prop("isChild") { it.isChild }.isEqualTo(true)
        prop("parent_id") { it.parent_id }.isEqualTo(parent)
      }
    }
    assertThat(transactionDao.row(children[1])?.category).isEqualTo(HOUSEHOLD)
    assertThat(transactionDao.childIds(listOf(parent)))
      .containsExactlyInAnyOrder(*children.toTypedArray())

    val parentMessages = messages().filter { it.row == parent.value }.map { it.column }
    assertThat(parentMessages.contains("category")).isFalse()
    assertThat(parentMessages.contains("isParent")).isTrue()
  }

  @Test
  fun `Off-budget transactions get no category`() = runWriterTest {
    insertAccount(OFF_BUDGET, offBudget = true)
    insertCategory(GROCERIES, name = "Groceries")

    val id = writer.write {
      insert(NewTransaction(account = OFF_BUDGET, date = DATE, category = GROCERIES))
    }

    assertThat(transactionDao.row(id)?.category).isNull()
  }

  @Test
  fun `Update only sends the fields that are set`() = runWriterTest {
    insertAccount(ACCOUNT)
    insertCategory(GROCERIES, name = "Groceries")
    val id = writer.write {
      insert(
        NewTransaction(
          account = ACCOUNT,
          date = DATE,
          amount = Amount(-500),
          category = GROCERIES,
          notes = "Old notes",
        )
      )
    }
    writer.write {
      update(
        TransactionUpdate(
          id = id,
          amount = Amount(-750),
          reconciled = true,
          category = Patch.To(null),
          notes = Patch.To("New notes"),
          importedId = Patch.To("bank-456"),
        )
      )
    }

    assertThat(lastSync())
      .containsExactly(
        Change(TRANSACTIONS, id.value, "category", Null),
        Change(TRANSACTIONS, id.value, "amount", number(-750)),
        Change(TRANSACTIONS, id.value, "notes", string("New notes")),
        Change(TRANSACTIONS, id.value, "financial_id", string("bank-456")),
        Change(TRANSACTIONS, id.value, "reconciled", number(1)),
      )
    assertThat(transactionDao.row(id)).isNotNull().all {
      prop("amount") { it.amount }.isEqualTo(Amount(-750))
      prop("category") { it.category }.isNull()
      prop("notes") { it.notes }.isEqualTo("New notes")
      prop("financial_id") { it.financial_id }.isEqualTo("bank-456")
      prop("reconciled") { it.reconciled }.isEqualTo(true)
      prop("date") { it.date }.isEqualTo(DATE)
      prop("acct") { it.acct }.isEqualTo(ACCOUNT)
    }
  }

  @Test
  fun `Moving a transaction off budget clears its category`() = runWriterTest {
    insertAccount(ACCOUNT)
    insertAccount(OFF_BUDGET, offBudget = true)
    insertCategory(GROCERIES, name = "Groceries")
    val id = writer.write {
      insert(NewTransaction(account = ACCOUNT, date = DATE, category = GROCERIES))
    }

    writer.write { update(TransactionUpdate(id = id, account = OFF_BUDGET)) }

    assertThat(transactionDao.row(id)).isNotNull().all {
      prop("acct") { it.acct }.isEqualTo(OFF_BUDGET)
      prop("category") { it.category }.isNull()
    }
  }

  @Test
  fun `Making a transaction a split parent clears its category`() = runWriterTest {
    insertAccount(ACCOUNT)
    insertCategory(GROCERIES, name = "Groceries")
    val id = writer.write {
      insert(NewTransaction(account = ACCOUNT, date = DATE, category = GROCERIES))
    }

    writer.write { update(TransactionUpdate(id = id, isParent = true)) }

    assertThat(transactionDao.row(id)).isNotNull().all {
      prop("isParent") { it.isParent }.isEqualTo(true)
      prop("category") { it.category }.isNull()
    }
  }

  @Test
  fun `Deleting a split parent deletes its children`() = runWriterTest {
    insertAccount(ACCOUNT)
    val (parent, child, other) =
      writer.write {
        val parent = insert(NewTransaction(account = ACCOUNT, date = DATE, isParent = true))
        val child = insert(NewTransaction(account = ACCOUNT, date = DATE, parentId = parent))
        val other = insert(NewTransaction(account = ACCOUNT, date = DATE))
        Triple(parent, child, other)
      }
    writer.write { delete(parent) }

    assertThat(lastSync())
      .containsExactly(
        Change(TRANSACTIONS, parent.value, "tombstone", number(1)),
        Change(TRANSACTIONS, child.value, "tombstone", number(1)),
      )
    assertThat(transactionDao.row(parent)?.tombstone).isEqualTo(true)
    assertThat(transactionDao.row(child)?.tombstone).isEqualTo(true)
    assertThat(transactionDao.row(other)?.tombstone).isEqualTo(false)
    assertThat(database.transactionsQueries.getIds().awaitAsList()).containsOnly(other)
  }

  @Test
  fun `Deleting a split parent added in the same batch deletes its children`() = runWriterTest {
    insertAccount(ACCOUNT)

    val child = writer.write {
      val parent = insert(NewTransaction(account = ACCOUNT, date = DATE, isParent = true))
      val child = insert(NewTransaction(account = ACCOUNT, date = DATE, parentId = parent))
      delete(parent)
      delete(child)
      child
    }

    assertThat(transactionDao.row(child)?.tombstone).isEqualTo(true)
    assertThat(messages().count { it.row == child.value && it.column == "tombstone" }).isEqualTo(1)
    assertThat(database.transactionsQueries.getIds().awaitAsList()).isEmpty()
  }

  @Test
  fun `Deleting a split parent deletes children attached in the same batch`() = runWriterTest {
    insertAccount(ACCOUNT)
    val (parent, child) =
      writer.write {
        val parent = insert(NewTransaction(account = ACCOUNT, date = DATE, isParent = true))
        val child = insert(NewTransaction(account = ACCOUNT, date = DATE))
        parent to child
      }

    writer.write {
      update(TransactionUpdate(id = child, parentId = Patch.To(parent)))
      delete(parent)
    }

    assertThat(transactionDao.row(parent)?.tombstone).isEqualTo(true)
    assertThat(transactionDao.row(child)?.tombstone).isEqualTo(true)
  }

  @Test
  fun `Deleting a split parent keeps children detached in the same batch`() = runWriterTest {
    insertAccount(ACCOUNT)
    val (parent, stored) =
      writer.write {
        val parent = insert(NewTransaction(account = ACCOUNT, date = DATE, isParent = true))
        val stored = insert(NewTransaction(account = ACCOUNT, date = DATE, parentId = parent))
        parent to stored
      }

    val (detached, moved) =
      writer.write {
        val other = insert(NewTransaction(account = ACCOUNT, date = DATE, isParent = true))
        val detached = insert(NewTransaction(account = ACCOUNT, date = DATE, parentId = parent))
        val moved = insert(NewTransaction(account = ACCOUNT, date = DATE, parentId = parent))
        update(TransactionUpdate(id = stored, parentId = Patch.To(null)))
        update(TransactionUpdate(id = detached, parentId = Patch.To(null)))
        update(TransactionUpdate(id = moved, parentId = Patch.To(other)))
        delete(parent)
        detached to moved
      }

    assertThat(transactionDao.row(parent)?.tombstone).isEqualTo(true)
    assertThat(transactionDao.row(stored)?.tombstone).isEqualTo(false)
    assertThat(transactionDao.row(detached)?.tombstone).isEqualTo(false)
    assertThat(transactionDao.row(moved)?.tombstone).isEqualTo(false)
  }

  @Test
  fun `Changing the parent of a transaction updates isChild`() = runWriterTest {
    insertAccount(ACCOUNT)
    val (parent, child) =
      writer.write {
        val parent = insert(NewTransaction(account = ACCOUNT, date = DATE, isParent = true))
        val child = insert(NewTransaction(account = ACCOUNT, date = DATE))
        parent to child
      }

    writer.write { update(TransactionUpdate(id = child, parentId = Patch.To(parent))) }
    assertThat(transactionDao.row(child)).isNotNull().all {
      prop("isChild") { it.isChild }.isEqualTo(true)
      prop("parent_id") { it.parent_id }.isEqualTo(parent)
    }
    assertThat(transactionDao.childIds(listOf(parent))).containsOnly(child)

    writer.write { update(TransactionUpdate(id = child, parentId = Patch.To(null))) }
    assertThat(transactionDao.row(child)).isNotNull().all {
      prop("isChild") { it.isChild }.isEqualTo(false)
      prop("parent_id") { it.parent_id }.isNull()
    }
  }

  @Test
  fun `Deleting more transactions than fit in one query`() = runWriterTest {
    insertAccount(ACCOUNT)
    val (parent, child) =
      writer.write {
        val parent = insert(NewTransaction(account = ACCOUNT, date = DATE, isParent = true))
        val child = insert(NewTransaction(account = ACCOUNT, date = DATE, parentId = parent))
        parent to child
      }
    val others = List(size = 2000) { TransactionId("other-$it") }

    writer.write { delete(others + parent) }

    assertThat(transactionDao.row(child)?.tombstone).isEqualTo(true)
  }

  @Test
  fun `Concurrent writes don't duplicate a payee`() = runWriterTest {
    val ids = coroutineScope {
      List(size = 2) {
          async {
            writer.write {
              val id = createPayee("Tesco")
              yield()
              id
            }
          }
        }
        .awaitAll()
    }

    assertThat(ids.distinct()).hasSize(1)
  }

  @Test
  fun `Insert a payee with its mapping`() = runWriterTest {
    val id = writer.write { insertPayee("Tesco") }

    assertThat(payeeDao[id]).isNotNull().all {
      prop("name") { it.name }.isEqualTo("Tesco")
      prop("tombstone") { it.tombstone }.isEqualTo(false)
    }
    assertThat(database.payeeMappingQueries.getIdsByTargetId(id).awaitAsList()).containsOnly(id)
    assertThat(lastSync())
      .containsExactly(
        Change(PAYEES, id.value, "name", string("Tesco")),
        Change(PAYEE_MAPPING, id.value, "targetId", string(id.value)),
      )
  }

  @Test
  fun `Insert a transfer payee`() = runWriterTest {
    insertAccount(ACCOUNT)
    val id = writer.write { insertPayee(name = "", transferAccount = ACCOUNT) }
    assertThat(payeeDao[id]?.transfer_acct).isEqualTo(ACCOUNT)
  }

  @Test
  fun `Create payee reuses a live payee with the same name`() = runWriterTest {
    val existing = writer.write { insertPayee("Tesco") }
    val deleted = PayeeId("deleted")
    database.payeesQueries.insert(
      id = deleted,
      name = "Aldi",
      category = null,
      tombstone = true,
      transfer_acct = null,
      favorite = false,
      learn_categories = null,
    )

    val (tesco, aldi, aldiAgain) =
      writer.write { Triple(createPayee("TESCO"), createPayee("Aldi"), createPayee("aldi")) }

    assertThat(tesco).isEqualTo(existing)
    assertThat(aldi).isEqualTo(aldiAgain)
    assertThat(payeeDao[aldi]?.name).isEqualTo("Aldi")
    assertThat(messages().count { it.dataset == PAYEES && it.column == "name" }).isEqualTo(2)
  }

  @Test
  fun `Insert a starting balance`() = runWriterTest {
    insertAccount(ACCOUNT)
    insertCategory(CategoryId("income"), name = "Income", isIncome = true)
    insertCategory(STARTING_BALANCES, name = "Starting Balances", isIncome = true)

    val (id, payee) =
      writer.write {
        insertStartingBalance(ACCOUNT, Amount(10_000), DATE) to startingBalancePayee()
      }

    assertThat(payee.category).isEqualTo(STARTING_BALANCES)
    assertThat(payeeDao[payee.id]?.name).isEqualTo("Starting Balance")
    assertThat(transactionDao.row(id)).isNotNull().all {
      prop("starting_balance_flag") { it.starting_balance_flag }.isEqualTo(true)
      prop("description") { it.description }.isEqualTo(payee.id)
      prop("category") { it.category }.isEqualTo(STARTING_BALANCES)
      prop("amount") { it.amount }.isEqualTo(Amount(10_000))
      prop("cleared") { it.cleared }.isEqualTo(true)
      prop("date") { it.date }.isEqualTo(DATE)
    }
  }

  @Test
  fun `Starting balance falls back to any income category, and none off budget`() = runWriterTest {
    insertAccount(OFF_BUDGET, offBudget = true)
    insertCategory(GROCERIES, name = "Groceries")
    insertCategory(CategoryId("income"), name = "Income", isIncome = true)

    val (payee, id) =
      writer.write {
        startingBalancePayee() to insertStartingBalance(OFF_BUDGET, Amount(500), DATE)
      }

    assertThat(payee.category).isEqualTo(CategoryId("income"))
    assertThat(transactionDao.row(id)?.category).isNull()
  }

  @Test
  fun `Update account sync fields`() = runWriterTest {
    insertAccount(ACCOUNT)

    writer.write {
      updateAccount(
        AccountUpdate(
          id = ACCOUNT,
          balanceCurrent = Patch.To(Amount(12_345)),
          lastSync = Patch.To(NOW),
          bankSyncStatus = Patch.To(BankSyncStatus.Ok),
        )
      )
    }

    assertThat(accountDao[ACCOUNT]).isNotNull().all {
      prop("balance_current") { it.balance_current }.isEqualTo(Amount(12_345))
      prop("last_sync") { it.last_sync }.isEqualTo(NOW)
      prop("bank_sync_status") { it.bank_sync_status }.isEqualTo(BankSyncStatus.Ok)
    }
    assertThat(lastSync())
      .containsExactly(
        Change(ACCOUNTS, ACCOUNT.value, "balance_current", number(12_345)),
        Change(ACCOUNTS, ACCOUNT.value, "last_sync", string(NOW.toEpochMilliseconds().toString())),
        Change(ACCOUNTS, ACCOUNT.value, "bank_sync_status", string("ok")),
      )

    writer.write {
      updateAccount(
        AccountUpdate(
          id = ACCOUNT,
          balanceCurrent = Patch.To(null),
          bankSyncStatus = Patch.To(null),
        )
      )
    }

    assertThat(accountDao[ACCOUNT]).isNotNull().all {
      prop("balance_current") { it.balance_current }.isNull()
      prop("last_sync") { it.last_sync }.isEqualTo(NOW)
      prop("bank_sync_status") { it.bank_sync_status }.isNull()
    }
  }

  @Test
  fun `Everything goes out in one sync call`() = runWriterTest {
    insertAccount(ACCOUNT)

    writer.write {
      val payee = createPayee("Tesco")
      val id = insert(NewTransaction(account = ACCOUNT, date = DATE, payee = payee))
      update(TransactionUpdate(id = id, amount = Amount(100)))
      insertStartingBalance(ACCOUNT, Amount(1000), DATE)
      updateAccount(AccountUpdate(id = ACCOUNT, bankSyncStatus = Patch.To(BankSyncStatus.Ok)))
    }

    assertThat(syncCalls.size).isEqualTo(1)
    assertThat(syncCalls.single().size).isEqualTo(messages().size)
  }

  @Test
  fun `An empty batch doesn't sync`() = runWriterTest {
    writer.write {}
    assertThat(syncCalls).isEmpty()
  }

  @Test
  fun `Dates before 1995 are rejected`() = runWriterTest {
    insertAccount(ACCOUNT)
    assertFailure {
      writer.write {
        insert(NewTransaction(account = ACCOUNT, date = LocalDate(1994, 12, 31)))
      }
    }
      .isInstanceOf<IllegalArgumentException>()
    assertThat(syncCalls).isEmpty()
  }

  @Test
  fun `Insert keeps the error and transfer fields`() = runWriterTest {
    insertAccount(ACCOUNT)
    val error = buildJsonObject { put("type", JsonPrimitive("SplitTransactionError")) }
    val transfer = TransactionId("transfer")

    val id = writer.write {
      insert(
        NewTransaction(
          account = ACCOUNT,
          date = DATE,
          error = error,
          transferId = transfer,
          sortOrder = 42,
          reconciled = true,
        )
      )
    }

    assertThat(transactionDao.row(id)).isNotNull().all {
      prop("error") { it.error }.isEqualTo(error)
      prop("transferred_id") { it.transferred_id }.isEqualTo(transfer)
      prop("sort_order") { it.sort_order }.isEqualTo(42.0)
      prop("reconciled") { it.reconciled }.isEqualTo(true)
    }
  }

  private companion object {
    val ACCOUNT = AccountId("account")
    val OFF_BUDGET = AccountId("off-budget")
    val GROCERIES = CategoryId("groceries")
    val HOUSEHOLD = CategoryId("household")
    val STARTING_BALANCES = CategoryId("starting-balances")
    val DATE = LocalDate(2026, 9, 15)
  }
}
