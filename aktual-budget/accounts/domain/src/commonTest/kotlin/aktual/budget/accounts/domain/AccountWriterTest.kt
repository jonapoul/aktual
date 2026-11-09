package aktual.budget.accounts.domain

import aktual.budget.BudgetSyncController
import aktual.budget.db.BudgetDatabase
import aktual.budget.db.buildDatabase
import aktual.budget.db.dao.AccountDao
import aktual.budget.db.dao.AccountGroupDao
import aktual.budget.db.dao.CategoryDao
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
import aktual.budget.transactions.domain.NewTransaction
import aktual.budget.transactions.domain.Patch.To
import aktual.budget.transactions.domain.TransactionWriter
import aktual.test.inMemoryDriverFactory
import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
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

    assertThat(writer.close(id)).isEqualTo(Deleted)

    assertThat(accountDao[id]?.tombstone).isEqualTo(true)
  }

  @Test
  fun `Closing an account with a zero balance only closes it`() = runWriterTest {
    val id = createAccount("Zero")
    addTransaction(id, Amount(500))
    addTransaction(id, Amount(-500))

    assertThat(writer.close(id)).isEqualTo(Closed)

    assertThat(accountDao[id]?.closed).isEqualTo(true)
    assertThat(accountDao.properties(id).transactionCount).isEqualTo(2L)
  }

  @Test
  fun `Closing moves the balance to the transfer account`() = runWriterTest {
    val closing = createAccount("Closing")
    val target = createAccount("Target")
    addTransaction(closing, Amount(1_234))

    assertThat(writer.close(closing, transferTo = target)).isEqualTo(Closed)

    assertThat(accountDao[closing]?.closed).isEqualTo(true)
    assertThat(accountDao.properties(closing).balance).isEqualTo(Zero)
    assertThat(accountDao.properties(target).balance).isEqualTo(Amount(1_234))
  }

  @Test
  fun `A non-zero balance needs a transfer account`() = runWriterTest {
    val id = createAccount("Closing")
    addTransaction(id, Amount(100))

    assertFailure { writer.close(id) }.isInstanceOf<IllegalArgumentException>()
    assertThat(accountDao[id]?.closed).isEqualTo(false)
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

    assertThat(writer.close(id)).isEqualTo(Unchanged)
    assertThat(syncCalls.size).isEqualTo(calls)
  }

  @Test
  fun `Reopening clears closed`() = runWriterTest {
    val id = createAccount("Closed")
    addTransaction(id, Zero)
    writer.close(id)

    writer.reopen(id)

    assertThat(accountDao[id]?.closed).isEqualTo(false)
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
  fun `Deleting a group ungroups its accounts`() = runWriterTest {
    val a = createAccount("A")
    val b = createAccount("B")
    val group = writer.createGroup("Everyday")
    writer.setGroup(a, group)
    writer.setGroup(b, group)

    writer.deleteGroup(group)

    assertThat(groupDao.getAllActive()).isEqualTo(emptyList())
    assertThat(accountDao[a]?.account_group_id).isNull()
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
  fun `Every change goes out through the sync controller`() = runWriterTest {
    createAccount("A")
    val calls = syncCalls.size

    writer.createGroup("Everyday")

    assertThat(syncCalls.size).isEqualTo(calls + 1)
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
