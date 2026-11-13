package aktual.budget.transactions.vm

import aktual.budget.model.AccountId
import aktual.budget.model.AccountSpec.SpecificAccount
import aktual.budget.model.Amount
import aktual.budget.model.TransactionId
import aktual.budget.model.localChange
import aktual.budget.transactions.vm.test.DATE_1
import aktual.budget.transactions.vm.test.DATE_2
import aktual.budget.transactions.vm.test.insertTransaction
import aktual.test.assertThatNextEmissionIsEqualTo
import androidx.paging.testing.asSnapshot
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEqualTo
import assertk.assertions.isNotNull
import kotlin.test.Test
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest

internal class ReconcileTest : TransactionsViewModelTestBase() {
  @Test
  fun `Reconciling at the cleared balance locks the cleared transactions`() = runTest {
    // given
    buildViewModel(SpecificAccount(AccountId("a")))
    with(transactions) {
      insertTransaction(id = "a", account = "a", category = "a", payee = "a")
      insertTransaction(id = "b", account = "a", category = "b", payee = "b", amount = -23.45)
      insertTransaction(id = "c", account = "b", category = "b", payee = "b")
    }
    sync.sendMessages(
      listOf(
        localChange("transactions", row = "a", "cleared", true),
        localChange("transactions", row = "b", "cleared", false),
        localChange("transactions", row = "c", "cleared", true),
      ),
    )
    advanceUntilIdle()

    // when
    viewModel.startReconciling(Amount(123.45))

    // then
    viewModel.reconciling
      .dropWhile { it?.cleared != Amount(123.45) }
      .test {
        assertThatNextEmissionIsEqualTo(Reconciling(Amount(123.45), cleared = Amount(123.45)))
      }

    // when
    viewModel.finishReconciling()
    awaitUntil { viewModel.reconciling.value == null }

    // then
    viewModel.reconciling.test { assertThatNextEmissionIsEqualTo(null) }
    assertThat(transactions.row(TransactionId("a"))?.reconciled).isEqualTo(true)
    assertThat(transactions.row(TransactionId("b"))?.reconciled).isNotEqualTo(true)
    assertThat(transactions.row(TransactionId("c"))?.reconciled).isNotEqualTo(true)
    assertThat(accounts[AccountId("a")]?.last_reconciled).isNotNull()
  }

  @Test
  fun `Exiting an unbalanced reconciliation locks nothing`() = runTest {
    // given
    buildViewModel(SpecificAccount(AccountId("a")))
    transactions.insertTransaction(id = "a", account = "a", category = "a", payee = "a")
    sync.sendMessages(listOf(localChange("transactions", row = "a", "cleared", true)))
    advanceUntilIdle()
    viewModel.startReconciling(Amount(100.00))
    viewModel.reconciling.first { it?.cleared == Amount(123.45) }

    // when
    viewModel.finishReconciling()
    awaitUntil { accounts[AccountId("a")]?.last_reconciled != null }

    // then
    assertThat(transactions.row(TransactionId("a"))?.reconciled).isNotEqualTo(true)
  }

  @Test
  fun `An adjustment makes up the difference`() = runTest {
    // given
    buildViewModel(SpecificAccount(AccountId("a")))
    transactions.insertTransaction(id = "a", account = "a", category = "a", payee = "a")
    sync.sendMessages(listOf(localChange("transactions", row = "a", "cleared", true)))
    advanceUntilIdle()
    viewModel.startReconciling(Amount(100.00))
    viewModel.reconciling
      .dropWhile { it?.cleared != Amount(123.45) }
      .test { assertThat(awaitItem()?.difference).isEqualTo(Amount(-23.45)) }

    // when
    viewModel.createReconcileAdjustment(notes = "Adjustment")

    // then
    viewModel.reconciling
      .dropWhile { it == null || it.isBusy || it.difference != Zero }
      .test {
        assertThatNextEmissionIsEqualTo(Reconciling(Amount(100.00), cleared = Amount(100.00)))
      }
    assertThat(viewModel.pagingData.asSnapshot().map { it.notes to it.amount })
      .containsExactlyInAnyOrder(null to Amount(123.45), "Adjustment" to Amount(-23.45))
  }

  @Test
  fun `Clearing a split clears its parts`() = runTest {
    // given
    buildViewModel(SpecificAccount(AccountId("a")))
    with(transactions) {
      insertTransaction(id = "p", account = "a", category = null, payee = "a", isParent = true)
      insertTransaction(id = "p1", account = "a", category = "a", payee = "a", parent = "p")
      insertTransaction(id = "p2", account = "a", category = "b", payee = "a", parent = "p")
    }

    // when
    viewModel.setCleared(TransactionId("p"), cleared = false)
    awaitUntil { transactions.row(TransactionId("p2"))?.cleared == false }

    // then
    for (id in listOf("p", "p1", "p2")) {
      assertThat(transactions.row(TransactionId(id))?.cleared).isEqualTo(false)
    }
  }

  @Test
  fun `Unlocking a split unlocks its parts`() = runTest {
    // given
    buildViewModel(SpecificAccount(AccountId("a")))
    with(transactions) {
      insertTransaction(id = "p", account = "a", category = null, payee = "a", isParent = true)
      insertTransaction(id = "p1", account = "a", category = "a", payee = "a", parent = "p")
    }
    sync.sendMessages(
      listOf(
        localChange("transactions", row = "p", "reconciled", true),
        localChange("transactions", row = "p1", "reconciled", true),
      ),
    )
    advanceUntilIdle()

    // when
    viewModel.unlockTransaction(TransactionId("p"))
    awaitUntil { transactions.row(TransactionId("p1"))?.reconciled == false }

    // then
    assertThat(transactions.row(TransactionId("p"))?.reconciled).isEqualTo(false)
    assertThat(transactions.row(TransactionId("p1"))?.reconciled).isEqualTo(false)
  }

  @Test
  fun `Reconciled rows show while reconciling`() = runTest {
    // given
    buildViewModel(SpecificAccount(AccountId("a")))
    with(transactions) {
      insertTransaction(id = "a", account = "a", category = "a", payee = "a", date = DATE_1)
      insertTransaction(id = "b", account = "a", category = "b", payee = "b", date = DATE_2)
    }
    sync.sendMessages(listOf(localChange("transactions", row = "a", "reconciled", true)))
    viewModel.setAccountOption(HideReconciledRows, enabled = true)
    viewModel.accountOptions.first { it?.hideReconciled == true }
    advanceUntilIdle()
    assertThat(viewModel.pagingData.asSnapshot().map { it.id.toString() }).containsExactly("b")

    // when
    viewModel.startReconciling(Amount(0.0))
    advanceUntilIdle()

    // then
    assertThat(viewModel.pagingData.asSnapshot().map { it.id.toString() to it.reconciled })
      .containsExactly("b" to false, "a" to true)
  }
}
