package aktual.budget.accounts.vm.manage

import aktual.budget.accounts.vm.AccountsTestScope
import aktual.budget.accounts.vm.runAccountsTest
import aktual.budget.model.AccountGroupId
import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.di.BudgetCoroutineScope
import aktual.test.viewModelContext
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
import kotlinx.coroutines.test.advanceUntilIdle

class ManageAccountsViewModelTest {
  @Test
  fun `Accounts are split by side then group, with Ungrouped first`() = runAccountsTest {
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
  fun `Closed accounts are listed apart and can be expanded`() = runAccountsTest {
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
  fun `Dropping an account into a group moves it and can be undone`() = runAccountsTest {
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
  fun `Dropping a group before another reorders the groups`() = runAccountsTest {
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
  fun `A taken group name sends an event and adds nothing`() = runAccountsTest {
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
  fun `Groups can be created, renamed and deleted`() = runAccountsTest {
    val viewModel = viewModel()
    scope.advanceUntilIdle()

    viewModel.createGroup(" Cards ")
    scope.advanceUntilIdle()
    val id = viewModel.successState().groups.single().id
    assertThat(viewModel.successState().groups.single().name).isEqualTo("Cards")

    viewModel.events.test {
      viewModel.renameGroup(id, "Credit cards")
      val renamed = awaitItem() as GroupRenamed
      assertThat(renamed.from to renamed.to).isEqualTo("Cards" to "Credit cards")
      scope.advanceUntilIdle()
      assertThat(viewModel.successState().groups.single().name).isEqualTo("Credit cards")

      viewModel.deleteGroup(id)
      val deleted = awaitItem() as GroupDeleted
      assertThat(deleted.name).isEqualTo("Credit cards")
      scope.advanceUntilIdle()
      assertThat(viewModel.successState().groups).isEqualTo(emptyList<AccountGroupItem>())

      viewModel.undo(deleted.undo)
      scope.advanceUntilIdle()
      assertThat(viewModel.successState().groups.single().name).isEqualTo("Credit cards")

      viewModel.undo(renamed.undo)
      scope.advanceUntilIdle()
      assertThat(viewModel.successState().groups.single().name).isEqualTo("Cards")
    }
  }

  @Test
  fun `Reopening moves an account back to the open list`() = runAccountsTest {
    val id = createAccount("Old card")
    writer.close(id)
    val viewModel = viewModel()
    scope.advanceUntilIdle()

    viewModel.events.test {
      viewModel.reopen(id)
      val event = awaitItem() as Reopened
      assertThat(event.name).isEqualTo("Old card")
      scope.advanceUntilIdle()
      assertThat(viewModel.successState().closed).isEqualTo(emptyList<ManagedAccount>())
      assertThat(viewModel.groupedIds()).containsExactly(null to listOf(id))

      viewModel.undo(event.undo)
      scope.advanceUntilIdle()
      assertThat(viewModel.successState().closed.map { it.id }).containsExactly(id)
    }
  }

  private fun AccountsTestScope.viewModel() =
    track(
      ManageAccountsViewModel(
        writer = writer,
        accountDao = accountDao,
        groupDao = groupDao,
        budgetScope = BudgetCoroutineScope(scope),
        viewModelContext = scope.viewModelContext,
      ),
    )

  private fun ManageAccountsViewModel.successState(): Success = state.value as Success

  private fun ManageAccountsViewModel.groupedIds(): List<Pair<AccountGroupId?, List<AccountId>>> =
    successState().onBudget.groups.map { group -> group.id to group.accounts.map { it.id } }

  private suspend fun ReceiveTurbine<ManageAccountsState>.awaitSuccess(): Success {
    while (true) {
      val item = awaitItem()
      if (item is Success) return item
    }
  }
}
