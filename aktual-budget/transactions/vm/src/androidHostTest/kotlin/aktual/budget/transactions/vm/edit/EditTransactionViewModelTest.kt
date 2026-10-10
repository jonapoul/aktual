package aktual.budget.transactions.vm.edit

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.dao.AccountDao
import aktual.budget.db.dao.CategoryDao
import aktual.budget.db.dao.PayeeDao
import aktual.budget.db.dao.TransactionDao
import aktual.budget.model.AccountId
import aktual.budget.model.CategoryId
import aktual.budget.model.PayeeId
import aktual.budget.model.TransactionId
import aktual.budget.transactions.domain.TransactionLoader
import aktual.budget.transactions.vm.asParent
import aktual.budget.transactions.vm.insertAccount
import aktual.budget.transactions.vm.insertCategory
import aktual.budget.transactions.vm.insertPayee
import aktual.budget.transactions.vm.insertTransaction
import aktual.budget.transactions.vm.transaction
import aktual.test.runDatabaseTest
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.AfterTest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class EditTransactionViewModelTest {
  @AfterTest
  fun after() {
    Dispatchers.resetMain()
  }

  @Test
  fun `A transaction opens in view mode`() = runDatabaseTest { scope ->
    insertEntities()
    TransactionDao(this).insertTransaction("t", account = "a", category = "a", payee = "a")
    val viewModel = createViewModel(scope, id = "t")

    viewModel.state.test {
      assertThat(awaitLoaded())
        .isEqualTo(
          EditTransactionState.Loaded(
            saved =
              TransactionDetails(
                transaction =
                  transaction("t", account = "a", category = "a", payee = "a", balance = 123.45),
                cleared = true,
                reconciled = false,
              ),
            mode = View,
            canEdit = true,
          ),
        )
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `A split part opens its whole split`() = runDatabaseTest { scope ->
    insertEntities()
    with(TransactionDao(this)) {
      insertTransaction("p", "a", null, "a", amount = 100.0, isParent = true)
      insertTransaction("p1", "a", "b", "b", amount = 60.0, parent = "p")
      insertTransaction("p2", "a", "c", "b", amount = 40.0, parent = "p")
    }
    val viewModel = createViewModel(scope, id = "p2")

    viewModel.state.test {
      assertThat(awaitLoaded().saved.transaction)
        .isEqualTo(
          transaction("p", "a", null, "a", amount = 100.0, balance = 100.0)
            .asParent(
              payee = "B&Q",
              transaction("p1", "a", "b", "b", amount = 60.0),
              transaction("p2", "a", "c", "b", amount = 40.0),
            ),
        )
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Changes made elsewhere are followed`() = runDatabaseTest { scope ->
    insertEntities()
    val transactions = TransactionDao(this)
    transactions.insertTransaction("p", "a", null, "a", amount = 100.0, isParent = true)
    transactions.insertTransaction("p1", "a", "b", "b", amount = 60.0, parent = "p")
    val viewModel = createViewModel(scope, id = "p")

    viewModel.state.test {
      assertThat(awaitLoaded().saved.transaction.children.size).isEqualTo(1)

      transactions.insertTransaction("p2", "a", "c", "b", amount = 40.0, parent = "p")
      scope.advanceUntilIdle()

      assertThat(awaitLoaded().saved.transaction.children.size).isEqualTo(2)
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `A missing transaction isn't found`() = runDatabaseTest { scope ->
    insertEntities()
    val viewModel = createViewModel(scope, id = "missing")

    viewModel.state.test {
      var state = awaitItem()
      while (state == Loading) state = awaitItem()
      assertThat(state).isEqualTo(EditTransactionState.Failure.NotFound)
      cancelAndIgnoreRemainingEvents()
    }
  }

  private suspend fun ReceiveTurbine<EditTransactionState>.awaitLoaded():
    EditTransactionState.Loaded {
    var state = awaitItem()
    while (state !is Loaded) state = awaitItem()
    return state
  }

  private suspend fun BudgetDatabase.insertEntities() {
    with(AccountDao(this)) {
      insertAccount(AccountId("a"), "Amex")
      insertAccount(AccountId("b"), "Barclays")
      insertAccount(AccountId("c"), "Chase")
    }
    with(PayeeDao(this)) {
      insertPayee(PayeeId("a"), "Argos")
      insertPayee(PayeeId("b"), "B&Q")
      insertPayee(PayeeId("c"), "Co-op")
    }
    with(CategoryDao(this)) {
      insertCategory(CategoryId("a"), "Additional")
      insertCategory(CategoryId("b"), "Building")
      insertCategory(CategoryId("c"), "Car")
    }
  }

  private fun BudgetDatabase.createViewModel(
    scope: TestScope,
    id: String,
  ): EditTransactionViewModel {
    Dispatchers.setMain(StandardTestDispatcher(scope.testScheduler))
    return EditTransactionViewModel(
      id = TransactionId(id),
      loader = TransactionLoader(TransactionDao(this)),
    )
  }
}
