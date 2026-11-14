package aktual.budget.accounts.domain

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.buildDatabase
import aktual.budget.db.dao.AccountDao
import aktual.budget.db.dao.AccountGroupDao
import aktual.budget.db.dao.AccountProperties
import aktual.budget.db.dao.CategoryDao
import aktual.budget.db.dao.DatabaseTables.ACCOUNTS
import aktual.budget.db.dao.NotesDao
import aktual.budget.db.dao.PayeeDao
import aktual.budget.db.dao.SyncDao
import aktual.budget.db.dao.TransactionDao
import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.Amount.Companion.Zero
import aktual.budget.model.BudgetId
import aktual.budget.model.LocalChange
import aktual.budget.model.SORT_INCREMENT
import aktual.budget.model.localChange
import aktual.budget.transactions.domain.NewTransaction
import aktual.budget.transactions.domain.Patch.To
import aktual.budget.transactions.domain.TransactionWriter
import aktual.budget.utils.BudgetSyncController
import aktual.test.inMemoryDriverFactory
import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isTrue
import kotlin.test.Test
import kotlin.time.Clock
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate

internal class AccountWriterTest {
  @Test
  fun `Closing an account with no transactions deletes it`() = runWriterTest {
    val id = createAccount("Empty")

    assertThat(writer.close(id).result).isEqualTo(Deleted)

    assertThat(accountDao[id]?.tombstone).isNotNull().isTrue()
  }

  @Test
  fun `Closing an account with a zero balance only closes it`() = runWriterTest {
    val id = createAccount("Zero")
    addTransaction(id, Amount(500))
    addTransaction(id, Amount(-500))

    assertThat(writer.close(id).result).isEqualTo(Closed)

    assertThat(accountDao[id]?.closed).isNotNull().isTrue()
    assertThat(accountDao.properties(id).transactionCount).isEqualTo(2L)
  }

  @Test
  fun `Closing moves the balance to the transfer account`() = runWriterTest {
    val closing = createAccount("Closing")
    val target = createAccount("Target")
    addTransaction(closing, Amount(1_234))

    assertThat(writer.close(closing, transferTo = target).result).isEqualTo(Closed)

    assertThat(accountDao[closing]?.closed).isNotNull().isTrue()
    assertThat(accountDao.properties(closing).balance).isEqualTo(Zero)
    assertThat(accountDao.properties(target).balance).isEqualTo(Amount(1_234))
  }

  @Test
  fun `A non-zero balance needs a transfer account`() = runWriterTest {
    val id = createAccount("Closing")
    addTransaction(id, Amount(100))

    assertFailure { writer.close(id) }.isInstanceOf<IllegalArgumentException>()
    assertThat(accountDao[id]?.closed).isNotNull().isFalse()
  }

  @Test
  fun `The balance can't go to the account being closed`() = runWriterTest {
    val id = createAccount("Closing")
    addTransaction(id, Amount(100))

    assertFailure { writer.close(id, transferTo = id) }.isInstanceOf<IllegalArgumentException>()
  }

  @Test
  fun `Closing a closed account does nothing`() = runWriterTest {
    val id = createAccount("Closed")
    addTransaction(id, Zero)
    writer.close(id)
    val calls = syncCalls.size

    assertThat(writer.close(id).result).isEqualTo(Unchanged)
    assertThat(syncCalls).hasSize(calls)
  }

  @Test
  fun `Undoing a close reopens the account and removes both sides of the transfer`() =
    runWriterTest {
      val closing = createAccount("Closing")
      val target = createAccount("Target")
      addTransaction(closing, Amount(1_234))

      val outcome = writer.close(closing, transferTo = target)
      writer.undo(outcome.undo)

      assertThat(accountDao[closing]?.closed).isNotNull().isFalse()
      assertThat(accountDao.properties(closing)).isEqualTo(AccountProperties(Amount(1_234), 1L))
      assertThat(accountDao.properties(target)).isEqualTo(AccountProperties(Zero, 0L))
    }

  @Test
  fun `Undoing a close that deleted an empty account restores it`() = runWriterTest {
    val id = createAccount("Empty")

    writer.undo(writer.close(id).undo)

    assertThat(accountDao[id]?.tombstone).isNotNull().isFalse()
  }

  @Test
  fun `Reopening clears closed`() = runWriterTest {
    val id = createAccount("Closed")
    addTransaction(id, Zero)
    writer.close(id)

    writer.reopen(id)

    assertThat(accountDao[id]?.closed).isNotNull().isFalse()
  }

  @Test
  fun `Undoing a reopen closes it again`() = runWriterTest {
    val id = createAccount("Closed")
    addTransaction(id, Zero)
    writer.close(id)

    writer.undo(writer.reopen(id))

    assertThat(accountDao[id]?.closed).isNotNull().isTrue()
  }

  @Test
  fun `Renaming changes the name`() = runWriterTest {
    val id = createAccount("Old")

    writer.rename(id, "New")

    assertThat(accountDao[id]?.name).isEqualTo("New")
  }

  @Test
  fun `Notes are stored under the account's note ID`() = runWriterTest {
    val id = createAccount("Noted")

    writer.setNotes(id, "Main bank")

    assertThat(NotesDao(database).getNote("account-${id.value}")).isEqualTo("Main bank")
  }

  @Test
  fun `Editing changes only what's given, and undoing puts it back`() = runWriterTest {
    val id = createAccount("Current")
    val group = writer.createGroup("Everyday")
    writer.setNotes(id, "Main bank")
    val calls = syncCalls.size

    val undo = writer.edit(id, name = To("Joint"), notes = To("Shared"), group = To(group))

    assertThat(syncCalls).hasSize(calls + 1)
    assertThat(accountDao[id]?.name).isEqualTo("Joint")
    assertThat(accountDao[id]?.account_group_id).isEqualTo(group)
    assertThat(writer.notes(id)).isEqualTo("Shared")

    writer.undo(undo)

    assertThat(accountDao[id]?.name).isEqualTo("Current")
    assertThat(accountDao[id]?.account_group_id).isNull()
    assertThat(writer.notes(id)).isEqualTo("Main bank")
  }

  @Test
  fun `Editing nothing sends nothing`() = runWriterTest {
    val id = createAccount("Current")
    val calls = syncCalls.size

    writer.undo(writer.edit(id))

    assertThat(syncCalls).hasSize(calls)
  }

  @Test
  fun `Setting a group and clearing it`() = runWriterTest {
    val id = createAccount("Grouped")
    val group = writer.createGroup("Everyday")

    writer.setGroup(id, group)
    assertThat(accountDao[id]?.account_group_id).isEqualTo(group)

    writer.setGroup(id, null)
    assertThat(accountDao[id]?.account_group_id).isNull()
  }

  @Test
  fun `Moving puts an account before the target on its side of the budget`() = runWriterTest {
    val a = createAccount("A")
    val b = createAccount("B")
    val c = createAccount("C")
    val off = createAccount("Off", offBudget = true)

    writer.move(c, target = a)

    assertThat(manageOrder(offBudget = false)).containsExactly(c, a, b)
    assertThat(manageOrder(offBudget = true)).containsExactly(off)
  }

  @Test
  fun `Moving without a target puts the account last and can change its group`() = runWriterTest {
    val a = createAccount("A")
    val b = createAccount("B")
    val group = writer.createGroup("Everyday")

    writer.move(a, target = null, group = To(group))

    assertThat(manageOrder(offBudget = false)).containsExactly(b, a)
    assertThat(accountDao[a]?.account_group_id).isEqualTo(group)
  }

  @Test
  fun `Undoing a move puts the account back in its place and group`() = runWriterTest {
    val a = createAccount("A")
    val b = createAccount("B")
    val group = writer.createGroup("Everyday")
    writer.setGroup(b, group)

    val undo = writer.move(b, target = a, group = To(null))
    writer.undo(undo)

    assertThat(manageOrder(offBudget = false)).containsExactly(a, b)
    assertThat(accountDao[b]?.account_group_id).isEqualTo(group)
  }

  @Test
  fun `Undoing a move restores the orders it shoved`() = runWriterTest {
    val a = createAccount("A")
    val b = createAccount("B")
    val c = createAccount("C")
    setSortOrder(a, 1.0)
    setSortOrder(b, 2.0)
    setSortOrder(c, 3.0)

    val undo = writer.move(c, target = b)
    assertThat(manageOrder(offBudget = false)).containsExactly(a, c, b)
    writer.undo(undo)

    assertThat(sortOrders()).isEqualTo(mapOf(a to 1.0, b to 2.0, c to 3.0))
  }

  @Test
  fun `New groups go after the last one`() = runWriterTest {
    val first = writer.createGroup("First")
    val second = writer.createGroup("Second")

    val groups = groupDao.getAllActive()
    assertThat(groups.map { it.id }).containsExactly(first, second)
    assertThat(groups.map { it.sort_order })
      .containsExactly(SORT_INCREMENT.toDouble(), SORT_INCREMENT * 2.0)
  }

  @Test
  fun `Group names are unique ignoring case`() = runWriterTest {
    writer.createGroup("Savings")

    assertFailure { writer.createGroup("SAVINGS") }.isInstanceOf<GroupNameTakenException>()
  }

  @Test
  fun `A group can be renamed to its own name but not another's`() = runWriterTest {
    val savings = writer.createGroup("Savings")
    writer.createGroup("Cards")

    writer.renameGroup(savings, "savings")
    assertThat(groupDao.getAllActive().first().name).isEqualTo("savings")

    assertFailure { writer.renameGroup(savings, "cards") }.isInstanceOf<GroupNameTakenException>()
  }

  @Test
  fun `Undoing a group rename puts the old name back`() = runWriterTest {
    val savings = writer.createGroup("Savings")

    writer.undo(writer.renameGroup(savings, "Rainy day"))

    assertThat(groupDao.getAllActive().single().name).isEqualTo("Savings")
  }

  @Test
  fun `Deleting a group ungroups its accounts`() = runWriterTest {
    val a = createAccount("A")
    val b = createAccount("B")
    val group = writer.createGroup("Everyday")
    writer.setGroup(a, group)
    writer.setGroup(b, group)

    writer.deleteGroup(group)

    assertThat(groupDao.getAllActive()).isEmpty()
    assertThat(accountDao[a]?.account_group_id).isNull()
    assertThat(accountDao[b]?.account_group_id).isNull()
  }

  @Test
  fun `Undoing a group delete restores it with its accounts`() = runWriterTest {
    val a = createAccount("A")
    val b = createAccount("B")
    val group = writer.createGroup("Everyday")
    writer.setGroup(a, group)

    writer.undo(writer.deleteGroup(group))

    assertThat(groupDao.getAllActive().map { it.id }).containsExactly(group)
    assertThat(accountDao[a]?.account_group_id).isEqualTo(group)
    assertThat(accountDao[b]?.account_group_id).isNull()
  }

  @Test
  fun `Moving a group puts it before the target`() = runWriterTest {
    val a = writer.createGroup("A")
    val b = writer.createGroup("B")
    val c = writer.createGroup("C")

    writer.moveGroup(c, target = a)

    assertThat(groupDao.getAllActive().map { it.id }).containsExactly(c, a, b)
  }

  @Test
  fun `Undoing a group move puts it back`() = runWriterTest {
    val a = writer.createGroup("A")
    val b = writer.createGroup("B")
    val c = writer.createGroup("C")

    writer.undo(writer.moveGroup(c, target = a))

    assertThat(groupDao.getAllActive().map { it.id }).containsExactly(a, b, c)
  }

  @Test
  fun `Every change goes out through the sync controller`() = runWriterTest {
    createAccount("A")
    val calls = syncCalls.size

    writer.createGroup("Everyday")

    assertThat(syncCalls).hasSize(calls + 1)
    assertThat(syncCalls.last().all { it.dataset == "account_groups" }).isTrue()
  }

  private class WriterTestScope(val database: BudgetDatabase, private val syncDao: SyncDao) :
    BudgetSyncController {
    val syncCalls = mutableListOf<List<LocalChange>>()
    val accountDao = AccountDao(database)
    val groupDao = AccountGroupDao(database)
    private var nextId = 1

    val transactions =
      TransactionWriter(
        syncController = this,
        accountDao = accountDao,
        categoryDao = CategoryDao(database),
        payeeDao = PayeeDao(database),
        transactionDao = TransactionDao(database),
        categoryLearner = {},
        uuidGenerator = ::uuid,
        clock = Clock.System,
      )

    val writer =
      AccountWriter(
        writer = transactions,
        syncController = this,
        accountDao = accountDao,
        groupDao = groupDao,
        payeeDao = PayeeDao(database),
        notesDao = NotesDao(database),
        uuidGenerator = ::uuid,
        calendar = { TODAY },
      )

    private fun uuid() = "id-${nextId++}"

    suspend fun createAccount(name: String, offBudget: Boolean = false): AccountId =
      transactions.write {
        createAccount(name, offBudget, Zero, TODAY)
      }

    suspend fun addTransaction(account: AccountId, amount: Amount) {
      transactions.write {
        insert(NewTransaction(account = account, date = TODAY, amount = amount))
      }
    }

    suspend fun manageOrder(offBudget: Boolean): List<AccountId> =
      accountDao.observeManageList().first().filter { it.offbudget == offBudget }.map { it.id }

    suspend fun setSortOrder(id: AccountId, order: Double) =
      syncChanges(listOf(localChange(ACCOUNTS, id.value, "sort_order", order)))

    suspend fun sortOrders(): Map<AccountId, Double?> =
      accountDao.observeManageList().first().associate { it.id to it.sort_order }

    override suspend fun syncChanges(changes: List<LocalChange>) {
      syncCalls.add(changes)
      syncDao.sendMessages(changes)
    }

    override fun schedule() = Unit
  }

  private fun runWriterTest(action: suspend WriterTestScope.() -> Unit) = runTest {
    val driver = inMemoryDriverFactory().create(BudgetId("abc-123"))
    driver.use {
      val database = buildDatabase(driver)
      WriterTestScope(database, SyncDao(database, driver, Clock.System)).action()
    }
  }

  private companion object {
    val TODAY = LocalDate(2026, 11, 9)
  }
}
