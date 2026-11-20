package aktual.budget.navrail.vm

import aktual.api.client.BudgetSyncApi
import aktual.budget.db.BudgetDatabase
import aktual.budget.db.dao.AccountDao
import aktual.budget.db.dao.AccountGroupDao
import aktual.budget.db.dao.TransactionDao
import aktual.budget.db.withoutResult
import aktual.budget.model.AccountId
import aktual.budget.model.AccountSyncSource
import aktual.budget.model.AccountSyncState
import aktual.budget.model.Amount
import aktual.budget.model.BudgetId
import aktual.budget.model.DbMetadata
import aktual.budget.model.DbMetadataKey.Companion.SidebarAccountsOpenState
import aktual.budget.model.SyncResponse
import aktual.budget.model.localChange
import aktual.budget.navrail.vm.SidebarAccountsAction.Search
import aktual.budget.navrail.vm.SidebarAccountsAction.ToggleSection
import aktual.budget.schedules.domain.UnpostedSchedules
import aktual.budget.utils.SyncStateHolder
import aktual.core.model.BudgetServer
import aktual.core.model.ServerUrl
import aktual.core.model.Token
import aktual.prefs.SystemUiPreferences
import aktual.prefs.SystemUiPreferencesImpl
import aktual.test.TestBudgetLocalPreferences
import aktual.test.TestBudgetSync
import aktual.test.assertThatNextEmissionIsEqualTo
import aktual.test.buildPreferences
import aktual.test.runDatabaseTest
import aktual.test.viewModelContext
import alakazam.test.unconfinedDispatcher
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import assertk.all
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import assertk.assertions.prop
import kotlin.test.Test
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.TestScope
import kotlinx.datetime.LocalDate
import okio.ByteString
import okio.IOException

class BudgetNavRailViewModelTest {
  private val prefs =
    TestBudgetLocalPreferences(DbMetadata(budgetName = "Household", cloudFileId = BUDGET_ID))
  private val sync = TestBudgetSync()
  private val api = TestBudgetSyncApi()
  private val unpostedSchedules = TestUnpostedSchedules()
  private val newTransactions = FakeNewTransactions()
  private val formatPreferences = FakeBudgetFormatPreferences()
  private val syncStateHolder = SyncStateHolder()

  @Test
  fun `Renaming updates the server, the local name and the sync log`() = runDatabaseTest { scope ->
    val viewModel = viewModel(REMOTE, scope)

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
  fun `Renaming without a server only changes the local name`() = runDatabaseTest { scope ->
    val viewModel = viewModel(None, scope)

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
  fun `API keys can be managed from a password session`() = runDatabaseTest { scope ->
    viewModel(REMOTE, scope).headerState.test {
      assertThat(awaitItem().canManageApiKeys).isTrue()
    }
  }

  @Test
  fun `API keys can't be managed from an API key session`() = runDatabaseTest { scope ->
    val server = REMOTE.copy(token = Token("actual_abc123"))
    viewModel(server, scope).headerState.test {
      assertThat(awaitItem().canManageApiKeys).isFalse()
    }
  }

  @Test
  fun `API keys can't be managed without a server`() = runDatabaseTest { scope ->
    viewModel(None, scope).headerState.test {
      assertThat(awaitItem().canManageApiKeys).isFalse()
    }
  }

  @Test
  fun `The name is kept if the server rejects the rename`() = runDatabaseTest { scope ->
    api.failure = IOException("Offline")
    val viewModel = viewModel(REMOTE, scope)

    viewModel.headerState.test {
      assertThat(awaitItem().budgetName).isEqualTo("Household")

      viewModel.rename("Renamed")
      expectNoEvents()

      assertThat(sync.changes).isEmpty()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Accounts are grouped with their balances`() = runDatabaseTest { scope ->
    insertAccount("on")
    insertAccount("off", offBudget = true)
    insertAccount("closed")
    accountsQueries.withoutResult { closeAccount(AccountId("closed")) }
    val transactions = TransactionDao(this)
    transactions.insert("t1", "on", "cat", "payee", DATE, amount = 10.0)
    transactions.insert("t2", "closed", "cat", "payee", DATE, amount = 3.0)
    val viewModel = viewModel(None, scope)

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
      assertThat(awaitItem().accounts).all {
        prop(DrawerAccounts::offBudget).isEqualTo(section(account("off", -25.0)))
        prop(DrawerAccounts::total).isEqualTo(Amount(-15.0))
      }
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Linked accounts carry their sync state`() = runDatabaseTest { scope ->
    insertAccount("unlinked")
    insertAccount("linked", syncSource = SimpleFin)
    val viewModel = viewModel(None, scope)

    viewModel.accounts.test {
      assertThat(awaitLoaded().onBudget.accounts.map { it.name to it.syncState })
        .containsExactly(
          "linked" to AccountSyncState.Ok(lastSync = null),
          "unlinked" to AccountSyncState.NotLinked,
        )
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Accounts carry their new transaction counts`() = runDatabaseTest { scope ->
    insertAccount("a")
    insertAccount("b")
    val viewModel = viewModel(None, scope)

    viewModel.accounts.test {
      assertThat(awaitLoaded().onBudget.accounts.map { it.newTransactions }).containsExactly(0, 0)

      newTransactions.counts.update { mapOf(AccountId("b") to 2) }
      assertThat(awaitItem().accounts.onBudget.accounts.map { it.name to it.newTransactions })
        .containsExactly("a" to 0, "b" to 2)

      newTransactions.markSeen(AccountId("b"))
      assertThat(awaitItem().accounts.onBudget.accounts.map { it.newTransactions })
        .containsExactly(0, 0)
    }
  }

  @Test
  fun `Sections start open apart from closed accounts`() = runDatabaseTest { scope ->
    insertAccount("a")
    viewModel(None, scope).accounts.test {
      assertThat(awaitLoadedState()).all {
        transform { it.isOpen(OnBudgetAccounts) }.isTrue()
        transform { it.isOpen(OffBudgetAccounts) }.isTrue()
        transform { it.isOpen(ClosedAccounts) }.isFalse()
        prop(SidebarAccountsState::allOpen).isTrue()
      }
    }
  }

  @Test
  fun `Toggling a section saves it to the budget's local prefs`() = runDatabaseTest { scope ->
    insertAccount("a")
    val viewModel = viewModel(None, scope)

    viewModel.accounts.test {
      awaitLoaded()

      viewModel.onAccountsAction(ToggleSection(OnBudgetAccounts))
      assertThat(awaitItem()).all {
        transform { it.isOpen(OnBudgetAccounts) }.isFalse()
        prop(SidebarAccountsState::allOpen).isFalse()
      }
      assertThat(prefs[SidebarAccountsOpenState]).isEqualTo(mapOf("onbudget" to false))

      viewModel.onAccountsAction(ToggleSection(ClosedAccounts))
      assertThat(awaitItem().isOpen(ClosedAccounts)).isTrue()
      assertThat(prefs[SidebarAccountsOpenState])
        .isEqualTo(mapOf("onbudget" to false, "closed" to true))
    }
  }

  @Test
  fun `Toggling all sections opens them all when any is collapsed`() = runDatabaseTest { scope ->
    insertAccount("a")
    val viewModel = viewModel(None, scope)

    viewModel.accounts.test {
      awaitLoaded()

      viewModel.onAccountsAction(ToggleAllSections)
      assertThat(awaitItem()).all {
        transform { it.isOpen(OnBudgetAccounts) }.isFalse()
        transform { it.isOpen(OffBudgetAccounts) }.isFalse()
      }

      viewModel.onAccountsAction(ToggleSection(OnBudgetAccounts))
      skipItems(1)
      viewModel.onAccountsAction(ToggleAllSections)
      assertThat(awaitItem()).all {
        prop(SidebarAccountsState::allOpen).isTrue()
        // Not part of expand/collapse all
        transform { it.isOpen(ClosedAccounts) }.isFalse()
      }
    }
  }

  @Test
  fun `Searching filters accounts and opens every section`() = runDatabaseTest { scope ->
    insertAccount("Current")
    insertAccount("Savings")
    val viewModel = viewModel(None, scope)

    viewModel.accounts.test {
      awaitLoaded()
      viewModel.onAccountsAction(ToggleSection(OnBudgetAccounts))
      skipItems(1)

      viewModel.onAccountsAction(Search(""))
      assertThat(awaitItem()).all {
        prop(SidebarAccountsState::search).isEqualTo("")
        prop(SidebarAccountsState::isSearching).isFalse()
      }

      viewModel.onAccountsAction(Search("sav"))
      assertThat(awaitItem()).all {
        transform { it.accounts.onBudget.accounts.map(DrawerAccount::name) }
          .containsExactly("Savings")
        transform { it.isOpen(OnBudgetAccounts) }.isTrue()
        // Totals still cover the whole section
        transform { it.accounts.onBudget.accountCount }.isEqualTo(2)
      }

      viewModel.onAccountsAction(Search(null))
      assertThat(awaitItem()).all {
        transform { it.accounts.onBudget.accounts.size }.isEqualTo(2)
        transform { it.isOpen(OnBudgetAccounts) }.isFalse()
      }
    }
  }

  @Test
  fun `Sync status follows the server and sync state`() = runDatabaseTest { scope ->
    viewModel(None, scope).headerState.test {
      assertThat(awaitItem().syncStatus).isEqualTo(LocalOnly)
    }

    viewModel(REMOTE, scope).headerState.test {
      assertThat(awaitItem().syncStatus).isEqualTo(Synced)

      syncStateHolder.update { Syncing }
      assertThat(awaitItem().syncStatus).isEqualTo(Syncing)

      syncStateHolder.update { ClockDrift }
      assertThat(awaitItem().syncStatus).isEqualTo(SyncError)
    }
  }

  private suspend fun ReceiveTurbine<SidebarAccountsState>.awaitLoadedState():
    SidebarAccountsState {
    var item = awaitItem()
    while (item.accounts == DrawerAccounts()) item = awaitItem()
    return item
  }

  private suspend fun ReceiveTurbine<SidebarAccountsState>.awaitLoaded(): DrawerAccounts =
    awaitLoadedState().accounts

  private fun account(id: String, balance: Double) =
    DrawerAccount(AccountId(id), name = id, balance = Amount(balance))

  private fun section(vararg accounts: DrawerAccount) =
    DrawerAccountSection(
      persistentListOf(DrawerAccountBucket(group = null, persistentListOf(*accounts))),
    )

  private suspend fun BudgetDatabase.insertAccount(
    id: String,
    offBudget: Boolean = false,
    syncSource: AccountSyncSource? = null,
  ) = accountsQueries.withoutResult {
    insert(
      id = AccountId(id),
      account_id = syncSource?.let { "remote-$id" },
      name = id,
      official_name = null,
      bank = null,
      offbudget = offBudget,
      account_sync_source = syncSource,
    )
  }

  @Test
  fun `Privacy mode is saved to the budget's synced prefs`() = runDatabaseTest { scope ->
    val viewModel = viewModel(None, scope)

    formatPreferences.isPrivacyEnabled.asFlow().test {
      assertThatNextEmissionIsEqualTo(false)

      viewModel.setPrivacyMode(isEnabled = true)
      assertThatNextEmissionIsEqualTo(true)

      viewModel.setPrivacyMode(isEnabled = false)
      assertThatNextEmissionIsEqualTo(false)
    }
  }

  @Test
  fun `Start tab follows the stored preference`() = runDatabaseTest { scope ->
    val systemUiPreferences =
      SystemUiPreferencesImpl(scope.buildPreferences(scope.unconfinedDispatcher))
    systemUiPreferences.startTab.set(Schedules)
    val viewModel = viewModel(None, scope, systemUiPreferences)

    viewModel.startTab.filterNotNull().test {
      assertThatNextEmissionIsEqualTo(Schedules)

      systemUiPreferences.startTab.set(Budget)
      assertThatNextEmissionIsEqualTo(Budget)
    }
  }

  private fun BudgetDatabase.viewModel(
    server: BudgetServer,
    scope: TestScope,
    systemUiPreferences: SystemUiPreferences =
      SystemUiPreferencesImpl(scope.buildPreferences(scope.unconfinedDispatcher)),
  ) =
    BudgetNavRailViewModel(
      contributors = emptySet(),
      localPreferences = prefs,
      server = server,
      syncApi = api,
      sync = sync,
      accountDao = AccountDao(this),
      groupDao = AccountGroupDao(this),
      newTransactions = newTransactions,
      syncStateHolder = syncStateHolder,
      formatPreferences = formatPreferences,
      unpostedSchedules = unpostedSchedules,
      systemUiPreferences = systemUiPreferences,
      viewModelContext = scope.viewModelContext,
    )

  private class TestUnpostedSchedules : UnpostedSchedules {
    override val hasUnposted = MutableStateFlow(false)

    override fun post() = Unit

    override fun dismiss() = Unit
  }

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
