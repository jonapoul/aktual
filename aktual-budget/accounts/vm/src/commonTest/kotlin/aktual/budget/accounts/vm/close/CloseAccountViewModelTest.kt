package aktual.budget.accounts.vm.close

import aktual.budget.accounts.vm.AccountsTestScope
import aktual.budget.accounts.vm.runAccountsTest
import aktual.budget.db.Accounts
import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.CategoryId
import aktual.test.viewModelContext
import app.cash.turbine.test
import assertk.all
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isTrue
import assertk.assertions.prop
import kotlin.test.Test
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle

class CloseAccountViewModelTest {
  @Test
  fun `The case follows the balance and transactions`() = runAccountsTest {
    val empty = createAccount("Empty", transactions = 0)
    val zero = createAccount("Zero")
    val balance = createAccount("Balance", balance = Amount(64_000))

    assertThat(viewModel(empty).ready().case).isEqualTo(NoTransactions)
    assertThat(viewModel(zero).ready().case).isEqualTo(ZeroBalance)
    assertThat(viewModel(balance).ready().case).isEqualTo(HasBalance)
  }

  @Test
  fun `Targets are the other open accounts`() = runAccountsTest {
    val closing = createAccount("Closing", balance = Amount(100))
    val open = createAccount("Open")
    val closed = createAccount("Closed")
    writer.close(closed)

    assertThat(viewModel(closing).ready().targets.map { it.id }).containsExactly(open)
  }

  @Test
  fun `A balance needs a target, and a category when it leaves the budget`() = runAccountsTest {
    val closing = createAccount("Closing", balance = Amount(100))
    val onBudget = createAccount("On")
    val offBudget = createAccount("Off", offBudget = true)
    categoryDao.insert(FOOD, "Food")
    val viewModel = viewModel(closing)
    assertThat(viewModel.ready().canClose).isFalse()

    viewModel.setTransferTo(onBudget)
    scope.advanceUntilIdle()
    assertThat(viewModel.ready()).all {
      prop(Ready::needsCategory).isFalse()
      prop(Ready::canClose).isTrue()
    }

    viewModel.setTransferTo(offBudget)
    scope.advanceUntilIdle()
    assertThat(viewModel.ready()).all {
      prop(Ready::needsCategory).isTrue()
      prop(Ready::canClose).isFalse()
    }

    viewModel.setCategory(FOOD)
    scope.advanceUntilIdle()
    assertThat(viewModel.ready()).all {
      prop(Ready::categoryName).isEqualTo("Food")
      prop(Ready::canClose).isTrue()
    }
  }

  @Test
  fun `Closing moves the balance and sends an undo`() = runAccountsTest {
    val closing = createAccount("Closing", balance = Amount(1_234))
    val target = createAccount("Target")
    val viewModel = viewModel(closing)
    viewModel.setTransferTo(target)
    viewModel.ready()

    lateinit var event: CloseAccountEvent.Closed
    viewModel.events.test {
      viewModel.close()
      event = awaitItem() as Closed
    }

    assertThat(event).all {
      prop(CloseAccountEvent.Closed::name).isEqualTo("Closing")
      prop(CloseAccountEvent.Closed::deleted).isFalse()
      prop(CloseAccountEvent.Closed::unlinkFailed).isFalse()
    }
    assertThat(accountDao[closing]?.closed).isEqualTo(true)
    assertThat(accountDao.properties(target).balance).isEqualTo(Amount(1_234))

    writer.undo(event.undo)
    assertThat(accountDao[closing]?.closed).isEqualTo(false)
    assertThat(accountDao.properties(target).balance).isEqualTo(Amount(0))
  }

  @Test
  fun `Closing an account with no transactions deletes it`() = runAccountsTest {
    val id = createAccount("Empty", transactions = 0)
    val viewModel = viewModel(id)
    viewModel.ready()

    lateinit var event: CloseAccountEvent.Closed
    viewModel.events.test {
      viewModel.close()
      event = awaitItem() as Closed
    }

    assertThat(event.deleted).isTrue()
    assertThat(accountDao[id]?.tombstone).isEqualTo(true)

    writer.undo(event.undo)
    assertThat(accountDao[id]?.tombstone).isEqualTo(false)
  }

  @Test
  fun `Closing a linked account unlinks it`() = runAccountsTest {
    val id = createAccount("Linked")
    link(id)
    val viewModel = viewModel(id)
    assertThat(viewModel.ready().isLinked).isTrue()

    viewModel.events.test {
      viewModel.close()
      awaitItem()
    }

    assertThat(accountDao[id]).isNotNull().all {
      prop(Accounts::closed).isEqualTo(true)
      prop(Accounts::account_id).isNull()
    }
  }

  @Test
  fun `Deleting removes the account and counts its transfers first`() = runAccountsTest {
    val other = createAccount("Other", balance = Amount(500))
    val id = createAccount("Deleting")
    writer.close(other, transferTo = id)
    val viewModel = viewModel(id)
    assertThat(viewModel.ready().transferCount).isEqualTo(1)

    viewModel.events.test {
      viewModel.delete()
      assertThat(awaitItem()).isEqualTo(CloseAccountEvent.Deleted("Deleting", unlinkFailed = false))
    }

    assertThat(accountDao[id]?.tombstone).isEqualTo(true)
    assertThat(accountDao.properties(other).transactionCount).isEqualTo(2L)
  }

  private fun AccountsTestScope.viewModel(id: AccountId) =
    track(
      CloseAccountViewModel(
        accountId = id,
        writer = writer,
        linker = linker,
        deleter = deleter,
        categoryDao = categoryDao,
        accountDao = accountDao,
        transactionDao = transactionDao,
        viewModelContext = scope.viewModelContext,
      ),
    )

  private suspend fun CloseAccountViewModel.ready(): Ready = state.filterIsInstance<Ready>().first()

  private companion object {
    val FOOD = CategoryId("food")
  }
}
