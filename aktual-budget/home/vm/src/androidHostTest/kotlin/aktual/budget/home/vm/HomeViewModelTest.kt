package aktual.budget.home.vm

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.dao.AccountDao
import aktual.budget.db.dao.TransactionDao
import aktual.budget.db.withoutResult
import aktual.budget.home.domain.AccountsSummaryLoader
import aktual.budget.home.vm.AccountsCardState.Loaded
import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.DbMetadata
import aktual.test.TestBudgetLocalPreferences
import aktual.test.runDatabaseTest
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import assertk.Assert
import assertk.all
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.prop
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class HomeViewModelTest {
  @AfterTest
  fun after() {
    Dispatchers.resetMain()
  }

  @Test
  fun `Budget name follows local preferences`() = runDatabaseTest { scope ->
    val prefs = TestBudgetLocalPreferences(DbMetadata(budgetName = "Household"))
    val viewModel = createViewModel(scope, prefs)

    viewModel.state.test {
      assertThat(awaitItem().budgetName).isEqualTo("Household")

      prefs += DbMetadata(budgetName = "Renamed")
      var state = awaitItem()
      while (state.budgetName != "Renamed") state = awaitItem()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `No open accounts is empty`() = runDatabaseTest { scope ->
    insertAccount("closed")
    accountsQueries.withoutResult { closeAccount(AccountId("closed")) }
    val viewModel = createViewModel(scope)

    viewModel.state.test { assertThat(awaitAccounts()).isEqualTo(Empty) }
  }

  @Test
  fun `Accounts summary updates when balances change`() = runDatabaseTest { scope ->
    insertAccount("a")
    insertAccount("b", offBudget = true)
    val transactions = TransactionDao(this)
    transactions.insert("t1", "a", "cat", "payee", DATE, amount = 10.0)
    val viewModel = createViewModel(scope)

    viewModel.state.test {
      assertThat(awaitLoaded()).hasBalances(a = 1_000L, b = 0L, netWorth = 1_000L)

      transactions.insert("t2", "b", "cat", "payee", DATE, amount = -25.0)
      assertThat(awaitLoaded()).hasBalances(a = 1_000L, b = -2_500L, netWorth = -1_500L)

      transactions.insert("t3", "a", "cat", "payee", DATE, amount = 5.5)
      assertThat(awaitLoaded()).hasBalances(a = 1_550L, b = -2_500L, netWorth = -950L)
    }
  }

  private fun Assert<Loaded>.hasBalances(a: Long, b: Long, netWorth: Long) =
    prop(Loaded::summary).all {
      transform { it.onBudget.accounts.map { account -> account.balance } }
        .containsExactly(Amount(a))
      transform { it.offBudget.accounts.map { account -> account.balance } }
        .containsExactly(Amount(b))
      transform { it.netWorth }.isEqualTo(Amount(netWorth))
    }

  private suspend fun ReceiveTurbine<HomeState>.awaitAccounts(): AccountsCardState {
    var accounts = awaitItem().accounts
    while (accounts == Loading) accounts = awaitItem().accounts
    return accounts
  }

  private suspend fun ReceiveTurbine<HomeState>.awaitLoaded() = awaitAccounts() as Loaded

  private fun BudgetDatabase.createViewModel(
    scope: TestScope,
    prefs: TestBudgetLocalPreferences = TestBudgetLocalPreferences(DbMetadata()),
  ): HomeViewModel {
    Dispatchers.setMain(StandardTestDispatcher(scope.testScheduler))
    return HomeViewModel(prefs, AccountsSummaryLoader(AccountDao(this)))
  }

  private suspend fun BudgetDatabase.insertAccount(id: String, offBudget: Boolean = false) =
    accountsQueries.withoutResult {
      insert(
        id = AccountId(id),
        account_id = null,
        name = id,
        official_name = null,
        bank = null,
        offbudget = offBudget,
        account_sync_source = null,
      )
    }

  private companion object {
    val DATE = LocalDate(2026, 1, 1)
  }
}
