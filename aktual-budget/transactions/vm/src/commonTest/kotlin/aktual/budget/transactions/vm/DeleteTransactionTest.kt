package aktual.budget.transactions.vm

import aktual.budget.model.AccountId
import aktual.budget.model.AccountSpec.SpecificAccount
import aktual.budget.model.TransactionId
import aktual.budget.transactions.vm.TransactionsEvent.TransactionDeleted
import aktual.budget.transactions.vm.test.insertTransaction
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isTrue
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

internal class DeleteTransactionTest : TransactionsViewModelTestBase() {
  @Test
  fun `Deleting a transaction tombstones it and leaves the others`() = runTest {
    // given
    buildViewModel(SpecificAccount(AccountId("a")))
    with(transactions) {
      insertTransaction(id = "x", account = "a", category = "a", payee = "a")
      insertTransaction(id = "y", account = "a", category = "a", payee = "a")
    }

    viewModel.events.test {
      // when
      viewModel.deleteTransaction(TransactionId("x"))

      // then
      assertThat(awaitItem()).isEqualTo(TransactionDeleted)
    }
    assertThat(transactions.row(TransactionId("x"))?.tombstone).isNotNull().isTrue()
    assertThat(transactions.row(TransactionId("y"))?.tombstone).isNotEqualTo(true)
  }

  @Test
  fun `Deleting a split parent deletes its parts`() = runTest {
    // given
    buildViewModel(SpecificAccount(AccountId("a")))
    with(transactions) {
      insertTransaction("p", "a", null, "a", amount = 100.0, isParent = true)
      insertTransaction("p1", "a", "b", "b", amount = 60.0, parent = "p")
      insertTransaction("p2", "a", "c", "b", amount = 40.0, parent = "p")
    }

    viewModel.events.test {
      // when
      viewModel.deleteTransaction(TransactionId("p"))

      // then
      assertThat(awaitItem()).isEqualTo(TransactionDeleted)
    }
    for (id in listOf("p", "p1", "p2")) {
      assertThat(transactions.row(TransactionId(id))?.tombstone).isNotNull().isTrue()
    }
  }
}
