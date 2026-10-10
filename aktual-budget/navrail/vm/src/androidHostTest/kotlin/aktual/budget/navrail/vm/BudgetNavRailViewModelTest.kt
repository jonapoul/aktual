package aktual.budget.navrail.vm

import aktual.api.client.BudgetSyncApi
import aktual.budget.db.BudgetDatabase
import aktual.budget.db.dao.AccountDao
import aktual.budget.db.dao.TransactionDao
import aktual.budget.db.withoutResult
import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.BudgetId
import aktual.budget.model.DbMetadata
import aktual.budget.model.SyncResponse
import aktual.budget.model.localChange
import aktual.core.model.BudgetServer
import aktual.core.model.ServerUrl
import aktual.core.model.Token
import aktual.test.TestBudgetLocalPreferences
import aktual.test.TestSyncController
import aktual.test.runDatabaseTest
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import assertk.all
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.prop
import kotlin.test.Test
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.LocalDate
import okio.ByteString
import okio.IOException
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class BudgetNavRailViewModelTest {
  private val prefs =
    TestBudgetLocalPreferences(DbMetadata(budgetName = "Household", cloudFileId = BUDGET_ID))
  private val sync = TestSyncController()
  private val api = TestBudgetSyncApi()

  @Test
  fun `Renaming updates the server, the local name and the sync log`() = runDatabaseTest {
    val viewModel = viewModel(REMOTE)

    viewModel.headerState.test {
      assertThat(awaitItem().budgetName).isEqualTo("Household")

      viewModel.rename("Renamed")
      assertThat(awaitItem().budgetName).isEqualTo("Renamed")

      assertThat(api.renames).containsExactly(BUDGET_ID to "Renamed")
      assertThat(sync.changes).containsExactly(NAME_CHANGE)
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Renaming without a server only changes the local name`() = runDatabaseTest {
    val viewModel = viewModel(None)

    viewModel.headerState.test {
      assertThat(awaitItem().budgetName).isEqualTo("Household")

      viewModel.rename("Renamed")
      assertThat(awaitItem().budgetName).isEqualTo("Renamed")

      assertThat(api.renames).isEmpty()
      assertThat(sync.changes).containsExactly(NAME_CHANGE)
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `The name is kept if the server rejects the rename`() = runDatabaseTest {
    api.failure = IOException("Offline")
    val viewModel = viewModel(REMOTE)

    viewModel.headerState.test {
      assertThat(awaitItem().budgetName).isEqualTo("Household")

      viewModel.rename("Renamed")
      expectNoEvents()

      assertThat(sync.changes).isEmpty()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Accounts are grouped with their balances`() = runDatabaseTest {
    insertAccount("on")
    insertAccount("off", offBudget = true)
    insertAccount("closed")
    accountsQueries.withoutResult { closeAccount(AccountId("closed")) }
    val transactions = TransactionDao(this)
    transactions.insert("t1", "on", "cat", "payee", DATE, amount = 10.0)
    transactions.insert("t2", "closed", "cat", "payee", DATE, amount = 3.0)
    val viewModel = viewModel(None)

    viewModel.accounts.test {
      assertThat(awaitLoaded())
        .isEqualTo(
          DrawerAccounts(
            onBudget = section(account("on", 10.0)),
            offBudget = section(account("off", 0.0)),
            closed = section(account("closed", 3.0)),
          ),
        )

      transactions.insert("t3", "off", "cat", "payee", DATE, amount = -25.0)
      assertThat(awaitItem()).all {
        prop(DrawerAccounts::offBudget).isEqualTo(section(account("off", -25.0)))
        prop(DrawerAccounts::total).isEqualTo(Amount(-15.0))
      }
      cancelAndIgnoreRemainingEvents()
    }
  }

  private suspend fun ReceiveTurbine<DrawerAccounts>.awaitLoaded(): DrawerAccounts {
    var item = awaitItem()
    while (item == DrawerAccounts()) item = awaitItem()
    return item
  }

  private fun account(id: String, balance: Double) =
    DrawerAccount(AccountId(id), name = id, balance = Amount(balance))

  private fun section(vararg accounts: DrawerAccount) =
    DrawerAccountSection(
      accounts = persistentListOf(*accounts),
      total = accounts.fold(Amount.Zero) { sum, account -> sum + account.balance },
    )

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

  private fun BudgetDatabase.viewModel(server: BudgetServer) =
    BudgetNavRailViewModel(
      contributors = emptySet(),
      localPreferences = prefs,
      server = server,
      syncApi = api,
      sync = sync,
      accountDao = AccountDao(this),
    )

  private class TestBudgetSyncApi : BudgetSyncApi {
    val renames = mutableListOf<Pair<BudgetId, String>>()
    var failure: Exception? = null

    override suspend fun syncBudget(requestBody: ByteString): SyncResponse = error("Unused")

    override suspend fun renameBudget(id: BudgetId, name: String) {
      failure?.let { throw it }
      renames += id to name
    }
  }

  private companion object {
    val DATE = LocalDate(2026, 1, 1)
    val BUDGET_ID = BudgetId("b328186c-c919-4333-959b-04e676c1ee46")
    val REMOTE = BudgetServer.Remote(ServerUrl(Https, "test.server.com"), Token("abc-123"))
    val NAME_CHANGE =
      localChange(
        dataset = "prefs",
        row = "budgetName",
        column = "value",
        value = "Renamed",
      )
  }
}
