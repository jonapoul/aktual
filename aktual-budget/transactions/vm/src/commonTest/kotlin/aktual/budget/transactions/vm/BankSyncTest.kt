package aktual.budget.transactions.vm

import aktual.budget.banksync.domain.BankSyncError
import aktual.budget.banksync.domain.BankSyncResult
import aktual.budget.banksync.domain.BankSyncSummary
import aktual.budget.db.Accounts
import aktual.budget.model.AccountId
import aktual.budget.model.AccountSpec.AllAccounts
import aktual.budget.model.AccountSpec.SpecificAccount
import aktual.budget.model.TransactionId
import aktual.budget.model.TransactionsSpec
import aktual.budget.transactions.vm.test.LINKED
import aktual.budget.transactions.vm.test.insertTransaction
import aktual.test.assertThatNextEmissionIsEqualTo
import androidx.lifecycle.viewModelScope
import app.cash.turbine.test
import assertk.all
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.prop
import kotlin.test.Test
import kotlin.uuid.Uuid
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest

internal class BankSyncTest : TransactionsViewModelTestBase() {
  @Test
  fun `Only a linked account can bank sync`() = runTest {
    // given
    buildViewModel(SpecificAccount(LINKED))
    val unlinked = factory.create(TransactionsSpec(SpecificAccount(AccountId("a"))))
    val all = factory.create(TransactionsSpec(AllAccounts))

    // The account query runs off the test scheduler, so wait for it before advancing
    listOf(viewModel, unlinked, all).forEach { vm -> vm.loadedAccount.first { it != Loading } }
    advanceUntilIdle()

    // then
    viewModel.canBankSync.test { assertThatNextEmissionIsEqualTo(true) }
    unlinked.canBankSync.test { assertThatNextEmissionIsEqualTo(false) }
    all.canBankSync.test { assertThatNextEmissionIsEqualTo(false) }

    unlinked.viewModelScope.cancel()
    all.viewModelScope.cancel()
  }

  @Test
  fun `Bank sync starts the controller for this account only`() = runTest {
    // given
    buildViewModel(SpecificAccount(LINKED))
    val all = factory.create(TransactionsSpec(AllAccounts))

    // when
    all.bankSync()

    // then
    assertThat(bankSync.started).isEmpty()

    // when
    viewModel.bankSync()

    // then
    assertThat(bankSync.started).containsExactly(setOf(LINKED))

    all.viewModelScope.cancel()
  }

  @Test
  fun `Is syncing while this account is pending`() = runTest {
    // given
    buildViewModel(SpecificAccount(LINKED))

    viewModel.isBankSyncing.test {
      assertThatNextEmissionIsEqualTo(false)

      // when
      bankSync.running(pending = listOf(AccountId("a"), LINKED))

      // then
      assertThatNextEmissionIsEqualTo(true)

      // when
      bankSync.running(pending = listOf(AccountId("a")))

      // then
      assertThatNextEmissionIsEqualTo(false)
    }
  }

  @Test
  fun `A finished sync only announces this account's results`() = runTest {
    // given
    buildViewModel(SpecificAccount(LINKED))
    val other = BankSyncResult.Failed(AccountId("a"), "Amex", BankSyncError.Internal(null))
    val synced =
      BankSyncResult.Synced(
        LINKED,
        "Linked",
        added = listOf(TransactionId("t")),
        updated = emptyList(),
      )

    viewModel.bankSyncFinished.test {
      // when
      bankSync.finish(other)
      bankSync.finish(other, synced)

      // then
      assertThatNextEmissionIsEqualTo(BankSyncSummary.Synced("Linked", added = 1, updated = 0))
    }
  }

  @Test
  fun `An open account's new transactions are marked read`() = runTest {
    // given
    buildViewModel(SpecificAccount(LINKED))
    val other = AccountId("a")

    newTransactions.counts.test {
      assertThatNextEmissionIsEqualTo(emptyMap())

      // when
      newTransactions.counts.update { mapOf(LINKED to 1, other to 1) }

      // then
      assertThatNextEmissionIsEqualTo(mapOf(LINKED to 1, other to 1))
      assertThatNextEmissionIsEqualTo(mapOf(other to 1))
    }
    assertThat(newTransactions.seen).containsExactly(LINKED)
  }

  @Test
  fun `Deleting an account unlinks it from its bank`() = runTest {
    // given
    buildViewModel(SpecificAccount(BANKED))
    accounts.insert(
      id = BANKED,
      accountId = "remote",
      name = "Banked",
      bank = Uuid.parse("00000000-0000-0000-0000-000000000001"),
      accountSyncSource = SimpleFin,
    )
    transactions.insertTransaction(id = "x", account = "banked", category = null, payee = "a")

    viewModel.events.test {
      // when
      viewModel.deleteAccount()

      // then
      assertThat(awaitItem()).isEqualTo(AccountDeleted)
    }
    assertThat(accounts[BANKED]).isNotNull().all {
      prop(Accounts::tombstone).isEqualTo(true)
      prop(Accounts::bank).isNull()
      prop(Accounts::account_id).isNull()
      prop(Accounts::account_sync_source).isNull()
    }
    assertThat(transactions.row(TransactionId("x"))?.tombstone).isEqualTo(true)
  }

  private companion object {
    val BANKED = AccountId("banked")
  }
}
