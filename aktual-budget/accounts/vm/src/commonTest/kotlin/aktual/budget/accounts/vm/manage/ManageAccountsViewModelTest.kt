package aktual.budget.accounts.vm.manage

import aktual.budget.BudgetSyncController
import aktual.budget.accounts.domain.AccountWriter
import aktual.budget.db.BudgetDatabase
import aktual.budget.db.buildDatabase
import aktual.budget.db.dao.AccountDao
import aktual.budget.db.dao.AccountGroupDao
import aktual.budget.db.dao.CategoryDao
import aktual.budget.db.dao.PayeeDao
import aktual.budget.db.dao.SyncDao
import aktual.budget.db.dao.TransactionDao
import aktual.budget.model.AccountGroupId
import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.Amount.Companion.Zero
import aktual.budget.model.BudgetId
import aktual.budget.model.LocalChange
import aktual.budget.transactions.domain.NewTransaction
import aktual.budget.transactions.domain.TransactionWriter
import aktual.di.BudgetCoroutineScope
import aktual.test.TestCalendar
import aktual.test.inMemoryDriverFactory
import aktual.test.viewModelContext
import androidx.lifecycle.viewModelScope
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import assertk.all
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isTrue
import assertk.assertions.prop
import kotlin.test.Test
import kotlin.time.Clock
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate

class ManageAccountsViewModelTest {
  @Test
  fun `Accounts are split by side then group, with Ungrouped first`() = runManageTest {
    val cash = createAccount("Cash", balance = Amount(4_000))
    val current = createAccount("Current", balance = Amount(120_433))
    val pension = createAccount("Pension", offBudget = true, balance = Amount(50_000))
    val everyday = writer.createGroup("Everyday")
    val empty = writer.createGroup("Investments")
    writer.setGroup(current, everyday)
    writer.setGroup(pension, everyday)

    viewModel().state.test {
      val state = awaitSuccess()
      assertThat(state.onBudget).all {
        prop(AccountSection::total).isEqualTo(Amount(124_433))
        prop(AccountSection::groups)
          .transform { groups -> groups.map { it.id to it.accounts.map(ManagedAccount::id) } }
          .containsExactly(
            null to listOf(cash),
            everyday to listOf(current),
            empty to emptyList<AccountId>(),
          )
      }
      assertThat(state.offBudget.groups.map { it.id to it.accounts.map(ManagedAccount::id) })
        .containsExactly(
          null to emptyList<AccountId>(),
          everyday to listOf(pension),
          empty to emptyList<AccountId>(),
        )
      assertThat(state.onBudget.groups[1].total).isEqualTo(Amount(120_433))
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Closed accounts are listed apart and can be expanded`() = runManageTest {
    val open = createAccount("Open")
    val closed = createAccount("Closed")
    writer.close(closed)
    val viewModel = viewModel()

    viewModel.state.test {
      val state = awaitSuccess()
      assertThat(state.closed.map { it.id }).containsExactly(closed)
      assertThat(state.onBudget.groups.flatMap { it.accounts }.map { it.id }).containsExactly(open)
      assertThat(state.closedExpanded).isFalse()

      viewModel.toggleClosed()
      assertThat(awaitSuccess().closedExpanded).isTrue()
    }
  }

  @Test
  fun `Dropping an account into a group moves it and can be undone`() = runManageTest {
    val a = createAccount("A")
    val b = createAccount("B")
    val group = writer.createGroup("Everyday")
    writer.setGroup(a, group)
    val viewModel = viewModel()
    scope.advanceUntilIdle()

    lateinit var event: ManageAccountsEvent.AccountMoved
    viewModel.events.test {
      viewModel.moveAccount(b, group = group, before = a)
      event = awaitItem() as AccountMoved
    }

    assertThat(event).all {
      prop(ManageAccountsEvent.AccountMoved::name).isEqualTo("B")
      prop(ManageAccountsEvent.AccountMoved::group).isEqualTo("Everyday")
    }
    scope.advanceUntilIdle()
    assertThat(viewModel.groupedIds())
      .containsExactly(null to emptyList<AccountId>(), group to listOf(b, a))

    viewModel.events.test {
      viewModel.undo(event.undo)
      scope.advanceUntilIdle()
      expectNoEvents()
    }
    assertThat(viewModel.groupedIds()).containsExactly(null to listOf(b), group to listOf(a))
  }

  @Test
  fun `Dropping a group before another reorders the groups`() = runManageTest {
    val first = writer.createGroup("First")
    val second = writer.createGroup("Second")
    val viewModel = viewModel()
    scope.advanceUntilIdle()

    viewModel.events.test {
      viewModel.moveGroup(second, before = first)
      assertThat(awaitItem()).isInstanceOf<ManageAccountsEvent.GroupMoved>()
    }

    scope.advanceUntilIdle()
    assertThat(viewModel.successState().groups.map { it.id }).containsExactly(second, first)
  }

  @Test
  fun `A taken group name sends an event and adds nothing`() = runManageTest {
    writer.createGroup("Savings")
    val viewModel = viewModel()
    scope.advanceUntilIdle()

    viewModel.events.test {
      viewModel.createGroup(" savings ")
      assertThat(awaitItem()).isEqualTo(ManageAccountsEvent.GroupNameTaken("Savings"))
    }

    assertThat(viewModel.successState().groups.map { it.name }).containsExactly("Savings")
    assertThat(viewModel.successState().isGroupNameTaken("SAVINGS")).isTrue()
  }

  @Test
  fun `Groups can be created, renamed and deleted`() = runManageTest {
    val viewModel = viewModel()
    scope.advanceUntilIdle()

    viewModel.createGroup(" Cards ")
    scope.advanceUntilIdle()
    val id = viewModel.successState().groups.single().id
    assertThat(viewModel.successState().groups.single().name).isEqualTo("Cards")

    viewModel.renameGroup(id, "Credit cards")
    scope.advanceUntilIdle()
    assertThat(viewModel.successState().groups.single().name).isEqualTo("Credit cards")

    viewModel.deleteGroup(id)
    scope.advanceUntilIdle()
    assertThat(viewModel.successState().groups).isEqualTo(emptyList<AccountGroupItem>())
  }

  @Test
  fun `Reopening moves an account back to the open list`() = runManageTest {
    val id = createAccount("Old card")
    writer.close(id)
    val viewModel = viewModel()
    scope.advanceUntilIdle()

    viewModel.events.test {
      viewModel.reopen(id)
      assertThat(awaitItem()).isEqualTo(ManageAccountsEvent.Reopened("Old card"))
    }

    scope.advanceUntilIdle()
    assertThat(viewModel.successState().closed).isEqualTo(emptyList<ManagedAccount>())
    assertThat(viewModel.groupedIds()).containsExactly(null to listOf(id))
  }

  private class ManageTestScope(
    val scope: TestScope,
    database: BudgetDatabase,
    private val syncDao: SyncDao,
  ) : BudgetSyncController {
    private val accountDao = AccountDao(database)
    private val groupDao = AccountGroupDao(database)
    private val payeeDao = PayeeDao(database)
    private var nextId = 1
    private val viewModels = mutableListOf<ManageAccountsViewModel>()

    val transactions =
      TransactionWriter(
        syncController = this,
        accountDao = accountDao,
        categoryDao = CategoryDao(database),
        payeeDao = payeeDao,
        transactionDao = TransactionDao(database),
        categoryLearner = {},
        uuidGenerator = { "id-${nextId++}" },
        clock = Clock.System,
      )

    val writer =
      AccountWriter(
        writer = transactions,
        syncController = this,
        accountDao = accountDao,
        groupDao = groupDao,
        payeeDao = payeeDao,
        uuidGenerator = { "id-${nextId++}" },
        calendar = TestCalendar(TODAY),
      )

    fun viewModel() =
      ManageAccountsViewModel(
          writer = writer,
          accountDao = accountDao,
          groupDao = groupDao,
          budgetScope = BudgetCoroutineScope(scope),
          viewModelContext = scope.viewModelContext,
        )
        .also(viewModels::add)

    suspend fun createAccount(
      name: String,
      offBudget: Boolean = false,
      balance: Amount = Zero,
    ): AccountId {
      val id = transactions.write { createAccount(name, offBudget, Zero, TODAY) }
      transactions.write { insert(NewTransaction(account = id, date = TODAY, amount = balance)) }
      return id
    }

    fun ManageAccountsViewModel.successState(): Success = state.value as Success

    fun ManageAccountsViewModel.groupedIds(): List<Pair<AccountGroupId?, List<AccountId>>> =
      successState().onBudget.groups.map { group -> group.id to group.accounts.map { it.id } }

    fun close() = viewModels.forEach { it.viewModelScope.cancel() }

    override suspend fun syncChanges(changes: List<LocalChange>) {
      syncDao.sendMessages(changes)
    }

    override fun schedule() = Unit
  }

  private fun runManageTest(action: suspend ManageTestScope.() -> Unit) = runTest {
    val driver = inMemoryDriverFactory().create(BudgetId("abc-123"))
    driver.use {
      val database = buildDatabase(driver)
      val testScope = ManageTestScope(this, database, SyncDao(database, driver, Clock.System))
      try {
        testScope.action()
      } finally {
        testScope.close()
      }
    }
  }

  private suspend fun ReceiveTurbine<ManageAccountsState>.awaitSuccess(): Success {
    while (true) {
      val item = awaitItem()
      if (item is Success) return item
    }
  }

  private companion object {
    val TODAY = LocalDate(2026, 11, 10)
  }
}
