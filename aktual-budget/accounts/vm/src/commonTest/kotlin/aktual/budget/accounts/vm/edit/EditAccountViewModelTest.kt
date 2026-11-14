package aktual.budget.accounts.vm.edit

import aktual.budget.accounts.vm.AccountsTestScope
import aktual.budget.accounts.vm.runAccountsTest
import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.transactions.domain.Patch.To
import aktual.di.BudgetCoroutineScope
import aktual.test.viewModelContext
import app.cash.turbine.test
import assertk.all
import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isNull
import assertk.assertions.isTrue
import assertk.assertions.prop
import kotlin.test.Test
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle

class EditAccountViewModelTest {
  @Test
  fun `Loads the account's name, notes and group`() = runAccountsTest {
    val id = createAccount("Current", balance = Amount(120_433))
    val group = writer.createGroup("Everyday")
    writer.edit(id, notes = To("Main bank"), group = To(group))

    val state = viewModel(id).editing()

    assertThat(state).all {
      prop(Editing::name).isEqualTo("Current")
      prop(Editing::notes).isEqualTo("Main bank")
      prop(Editing::group).isEqualTo(group)
      prop(Editing::balance).isEqualTo(Amount(120_433))
      prop(Editing::transactionCount).isEqualTo(1L)
      prop(Editing::canSave).isFalse()
    }
  }

  @Test
  fun `A name another account has, even a closed one, can't be saved`() = runAccountsTest {
    val id = createAccount("Current")
    val other = createAccount("Old card")
    writer.close(other)
    val viewModel = viewModel(id)
    viewModel.editing()

    viewModel.setName(" Old card ")
    scope.advanceUntilIdle()
    assertThat(viewModel.editing()).all {
      prop(Editing::duplicateName).isEqualTo("Old card")
      prop(Editing::canSave).isFalse()
    }

    viewModel.setName("old card")
    scope.advanceUntilIdle()
    assertThat(viewModel.editing().canSave).isTrue()
  }

  @Test
  fun `Saving writes the edits and sends an undo`() = runAccountsTest {
    val id = createAccount("Current")
    val group = writer.createGroup("Everyday")
    val viewModel = viewModel(id)
    viewModel.editing()

    viewModel.setName(" Joint ")
    viewModel.setNotes("Shared")
    viewModel.setGroup(group)
    lateinit var event: EditAccountEvent.Saved
    viewModel.events.test {
      viewModel.save()
      event = awaitItem() as Saved
    }

    assertThat(event).all {
      prop(EditAccountEvent.Saved::name).isEqualTo("Joint")
      prop(EditAccountEvent.Saved::renamedFrom).isEqualTo("Current")
    }
    assertThat(accountDao[id]?.name).isEqualTo("Joint")
    assertThat(accountDao[id]?.account_group_id).isEqualTo(group)
    assertThat(writer.notes(id)).isEqualTo("Shared")

    viewModel.undo(event.undo)
    scope.advanceUntilIdle()
    assertThat(accountDao[id]?.name).isEqualTo("Current")
    assertThat(accountDao[id]?.account_group_id).isNull()
    assertThat(writer.notes(id)).isEmpty()
  }

  @Test
  fun `Saving only notes isn't a rename`() = runAccountsTest {
    val id = createAccount("Current")
    val viewModel = viewModel(id)
    viewModel.editing()

    viewModel.setNotes("Main bank")
    viewModel.events.test {
      viewModel.save()
      assertThat(awaitItem())
        .isInstanceOf<EditAccountEvent.Saved>()
        .prop(EditAccountEvent.Saved::renamedFrom)
        .isNull()
    }
  }

  @Test
  fun `A later reload keeps what's been typed`() = runAccountsTest {
    val id = createAccount("Current")
    val viewModel = viewModel(id)
    viewModel.editing()

    viewModel.setName("Typed")
    writer.createGroup("Everyday")
    scope.advanceUntilIdle()

    assertThat(viewModel.editing()).all {
      prop(Editing::name).isEqualTo("Typed")
      prop(Editing::groups).transform { it.map { g -> g.name } }.isEqualTo(listOf("Everyday"))
    }
  }

  @Test
  fun `A missing account fails to load`() = runAccountsTest {
    val viewModel = viewModel(AccountId("missing"))
    scope.advanceUntilIdle()

    assertThat(viewModel.state.value).isInstanceOf<Failure>()
  }

  private fun AccountsTestScope.viewModel(id: AccountId) =
    track(
      EditAccountViewModel(
        accountId = id,
        writer = writer,
        accountDao = accountDao,
        groupDao = groupDao,
        budgetScope = BudgetCoroutineScope(scope),
        viewModelContext = scope.viewModelContext,
      ),
    )

  private suspend fun EditAccountViewModel.editing(): Editing =
    state.filterIsInstance<Editing>().first()
}
