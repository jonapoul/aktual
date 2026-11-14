package aktual.budget.transactions.vm

import aktual.budget.db.dao.ClearedBalances
import aktual.budget.model.AccountId
import aktual.budget.model.AccountSpec.AllAccounts
import aktual.budget.model.AccountSpec.Group
import aktual.budget.model.AccountSpec.SpecificAccount
import aktual.budget.model.Amount
import aktual.budget.model.TagId
import aktual.budget.model.TagSpec
import aktual.budget.model.TransactionsSpec
import aktual.budget.model.localChange
import aktual.budget.transactions.vm.test.DATE_1
import aktual.budget.transactions.vm.test.DATE_2
import aktual.budget.transactions.vm.test.LINKED
import aktual.budget.transactions.vm.test.insertTransaction
import aktual.test.assertThatNextEmissionIsEqualTo
import androidx.lifecycle.viewModelScope
import androidx.paging.testing.asSnapshot
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import kotlin.test.Test
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest

internal class AccountOptionsTest : TransactionsViewModelTestBase() {
  @Test
  fun `Only a single account's list has account options`() = runTest {
    // given
    buildViewModel(AllAccounts)
    advanceUntilIdle()

    // then
    viewModel.accountOptions.test { assertThatNextEmissionIsEqualTo(null) }
    viewModel.showRunningBalance.test { assertThatNextEmissionIsEqualTo(true) }
  }

  @Test
  fun `A single account's list offers the open accounts to switch to`() = runTest {
    // given
    buildViewModel(SpecificAccount(AccountId("a")))

    // then
    viewModel.accountChoices
      .dropWhile { it.isEmpty() }
      .test {
        assertThatNextEmissionIsEqualTo(
          persistentListOf(
            AccountChoice(AccountId("a"), "Amex"),
            AccountChoice(AccountId("b"), "Barclays"),
            AccountChoice(AccountId("c"), "Chase"),
            AccountChoice(LINKED, "Linked"),
          ),
        )
      }
  }

  @Test
  fun `Every account's list offers the open accounts to switch to`() = runTest {
    // given
    buildViewModel(AllAccounts)

    // then
    viewModel.accountChoices
      .dropWhile { it.isEmpty() }
      .test {
        assertThatNextEmissionIsEqualTo(
          persistentListOf(
            AccountChoice(AccountId("a"), "Amex"),
            AccountChoice(AccountId("b"), "Barclays"),
            AccountChoice(AccountId("c"), "Chase"),
            AccountChoice(LINKED, "Linked"),
          ),
        )
      }
  }

  @Test
  fun `A group's list has no accounts to switch to`() = runTest {
    // given
    buildViewModel(Group(OnBudget))
    advanceUntilIdle()

    // then
    viewModel.accountChoices.test { assertThatNextEmissionIsEqualTo(persistentListOf()) }
  }

  @Test
  fun `Account options are off until set`() = runTest {
    // given
    buildViewModel(SpecificAccount(AccountId("a")))

    viewModel.accountOptions
      .dropWhile { it == null }
      .test {
        assertThatNextEmissionIsEqualTo(AccountViewOptions(false, false, false))

        // when
        viewModel.setAccountOption(RunningBalance, enabled = true)

        // then
        assertThatNextEmissionIsEqualTo(AccountViewOptions(true, false, false))
      }
    viewModel.showRunningBalance.test { assertThatNextEmissionIsEqualTo(true) }
  }

  @Test
  fun `Hiding reconciled transactions drops them from the list`() = runTest {
    // given
    buildViewModel(SpecificAccount(AccountId("a")))
    with(transactions) {
      insertTransaction(id = "a", account = "a", category = "a", payee = "a", date = DATE_1)
      insertTransaction(id = "b", account = "a", category = "b", payee = "b", date = DATE_2)
    }
    sync.sendMessages(listOf(localChange("transactions", row = "a", "reconciled", true)))
    advanceUntilIdle()
    assertThat(viewModel.pagingData.asSnapshot().map { it.id.toString() }).containsExactly("b", "a")

    // when
    viewModel.setAccountOption(HideReconciledRows, enabled = true)
    // The pref is read off the test scheduler
    viewModel.accountOptions.first { it?.hideReconciled == true }
    advanceUntilIdle()

    // then
    assertThat(viewModel.pagingData.asSnapshot().map { it.id.toString() to it.balance })
      .containsExactly("b" to Amount(246.90))
  }

  @Test
  fun `Extra balances split the account by cleared state`() = runTest {
    // given
    buildViewModel(SpecificAccount(AccountId("a")))
    with(transactions) {
      insertTransaction(id = "a", account = "a", category = "a", payee = "a")
      insertTransaction(id = "b", account = "a", category = "b", payee = "b", amount = -23.45)
    }
    sync.sendMessages(
      listOf(
        localChange("transactions", row = "a", "cleared", true),
        localChange("transactions", row = "b", "cleared", false),
      ),
    )
    advanceUntilIdle()
    viewModel.clearedBalances.test { assertThatNextEmissionIsEqualTo(null) }

    // when
    viewModel.setAccountOption(ExtraBalances, enabled = true)

    // then
    viewModel.clearedBalances
      .dropWhile { it == null }
      .test {
        assertThatNextEmissionIsEqualTo(
          ClearedBalances(cleared = 12_345L, uncleared = -2_345L),
        )
      }
  }

  @Test
  fun `Balance follows the transactions in view`() = runTest {
    // given
    buildViewModel(SpecificAccount(AccountId("a")))
    with(transactions) {
      insertTransaction(id = "a", account = "a", category = "a", payee = "a")
      insertTransaction(id = "b", account = "b", category = "b", payee = "b")
    }
    advanceUntilIdle()

    // The balance query runs off the test scheduler, so the state can still hold a value from
    // before the inserts
    viewModel.balance
      .dropWhile { it != Amount(123.45) }
      .test {
        assertThatNextEmissionIsEqualTo(Amount(123.45))

        // when
        transactions.insertTransaction("c", "a", "c", "c", date = DATE_2, amount = -23.45)

        // then
        assertThatNextEmissionIsEqualTo(Amount(100.0))
      }
  }

  @Test
  fun `Tag-filtered lists show no balance`() = runTest {
    // given
    buildViewModel(AllAccounts)
    transactions.insertTransaction(id = "a", account = "a", category = "a", payee = "a")
    advanceUntilIdle()

    // when
    val tagged = factory.create(TransactionsSpec(tagSpec = TagSpec.SpecificTag(TagId("food"))))

    // then
    tagged.showBalance.test { assertThatNextEmissionIsEqualTo(false) }
    tagged.balance.test { assertThatNextEmissionIsEqualTo(null) }
    tagged.viewModelScope.cancel()
  }

  @Test
  fun `Density defaults to compact and is remembered`() = runTest {
    // given
    buildViewModel(AllAccounts)

    viewModel.density.test {
      assertThatNextEmissionIsEqualTo(Compact)

      // when
      viewModel.setDensity(Dense)

      // then
      assertThatNextEmissionIsEqualTo(Dense)
    }

    // and a new view model picks it up
    val other = factory.create(TransactionsSpec(AllAccounts))
    other.density.test { assertThatNextEmissionIsEqualTo(Dense) }
    other.viewModelScope.cancel()
  }
}
