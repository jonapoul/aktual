package aktual.budget.navrail.vm

import aktual.api.client.BudgetSyncApi
import aktual.budget.banksync.domain.NewTransactions
import aktual.budget.db.dao.AccountDao
import aktual.budget.db.dao.AccountGroupDao
import aktual.budget.model.BudgetTab
import aktual.budget.model.DbMetadataKey.Companion.BudgetName
import aktual.budget.model.DbMetadataKey.Companion.CloudFileId
import aktual.budget.model.DbMetadataKey.Companion.SidebarAccountsOpenState
import aktual.budget.model.SyncState
import aktual.budget.model.localChange
import aktual.budget.schedules.domain.UnpostedSchedules
import aktual.budget.utils.BudgetLocalPreferences
import aktual.budget.utils.BudgetSync
import aktual.budget.utils.SyncStateHolder
import aktual.core.model.BudgetServer
import aktual.core.nav.BudgetNavEntryContributor
import aktual.core.utils.runCatchingCancellable
import aktual.di.BudgetScope
import aktual.di.ViewModelContext
import aktual.di.newScope
import aktual.log
import aktual.prefs.BudgetFormatPreferences
import aktual.prefs.SystemUiPreferences
import aktual.prefs.asStateFlow
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.molecule.launchMolecule
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Stable
@Suppress("LongParameterList") // One source per part of the sidebar
@ViewModelKey
@ContributesIntoMap(BudgetScope::class)
class BudgetNavRailViewModel
internal constructor(
  contributors: Set<BudgetNavEntryContributor>,
  private val localPreferences: BudgetLocalPreferences,
  private val server: BudgetServer,
  private val syncApi: BudgetSyncApi,
  private val sync: BudgetSync,
  accountDao: AccountDao,
  groupDao: AccountGroupDao,
  newTransactions: NewTransactions,
  syncStateHolder: SyncStateHolder,
  private val formatPreferences: BudgetFormatPreferences,
  private val unpostedSchedules: UnpostedSchedules,
  systemUiPreferences: SystemUiPreferences,
  viewModelContext: ViewModelContext,
) : ViewModel(viewModelContext.newScope()) {
  val budgetNavEntryContributors: ImmutableSet<BudgetNavEntryContributor> =
    contributors.toImmutableSet()

  val headerState: StateFlow<DrawerHeaderState> =
    viewModelScope.launchMolecule(Immediate) {
      val budgetNameFlow = remember { localPreferences.observe(BudgetName) }
      val budgetName by budgetNameFlow.collectAsState(initial = localPreferences[BudgetName])
      val syncState by syncStateHolder.collectAsState()
      val remote = server as? Remote
      DrawerHeaderState(
        budgetName = budgetName,
        serverHost = remote?.url?.baseUrl,
        // Keys can't manage other keys, and the budget needs to be on the server
        canManageApiKeys =
          remote != null && !remote.token.isApiKey && localPreferences[CloudFileId] != null,
        syncStatus = syncStatus(remote != null, syncState),
      )
    }

  val startTab: StateFlow<BudgetTab> = systemUiPreferences.startTab.asStateFlow(viewModelScope)

  private val search = MutableStateFlow<String?>(null)

  private val drawerAccounts =
    combine(
        accountDao.observeAllWithBalances(),
        groupDao.observeAllActive(),
        newTransactions.counts,
      ) { rows, groups, counts ->
        rows.toDrawerAccounts(groups, counts)
      }
      .catch { e -> log.e(e) { "Failed loading accounts" } }

  private val openSections = localPreferences.observe(SidebarAccountsOpenState)

  val accounts: StateFlow<SidebarAccountsState> =
    combine(drawerAccounts, search, openSections) { accounts, search, openSections ->
        SidebarAccountsState(
          accounts = accounts.filter(search.orEmpty()),
          search = search,
          openSections = openSections.orEmpty().toImmutableMap(),
          toggleableSections = accounts.toggleableSections(),
        )
      }
      .stateIn(viewModelScope, Eagerly, initialValue = SidebarAccountsState())

  val hasUnpostedSchedules: StateFlow<Boolean> = unpostedSchedules.hasUnposted

  fun onAccountsAction(action: SidebarAccountsAction) {
    when (action) {
      is ToggleSection -> {
        val isOpen = accounts.value.isOpen(action.section)
        saveOpenSections(mapOf(action.section.key to !isOpen))
      }
      ToggleAllSections -> {
        val state = accounts.value
        val open = !state.allOpen
        saveOpenSections(state.toggleableSections.associate { it.key to open })
      }
      is Search -> search.update { action.query }
    }
  }

  private fun saveOpenSections(changes: Map<String, Boolean>) {
    localPreferences.update { metadata ->
      val current = metadata[SidebarAccountsOpenState].orEmpty()
      metadata.set(SidebarAccountsOpenState, current + changes)
    }
  }

  fun postUnpostedSchedules() = unpostedSchedules.post()

  fun dismissUnpostedSchedules() = unpostedSchedules.dismiss()

  fun setPrivacyMode(isEnabled: Boolean) {
    viewModelScope.launch { formatPreferences.isPrivacyEnabled.set(isEnabled) }
  }

  fun rename(name: String) {
    viewModelScope.launch {
      runCatchingCancellable(
        attempt = {
          // The server keeps its own copy of the name for the budget list, see saveMetadataPrefs in
          // packages/loot-core/src/server/preferences/app.ts
          val cloudFileId = localPreferences[CloudFileId]
          if (server is Remote && cloudFileId != null) {
            syncApi.renameBudget(cloudFileId, name)
          }
          localPreferences.update { metadata -> metadata.set(BudgetName, name) }
          sync(budgetNameChange(name))
        },
        onFailure = { e -> log.e(e) { "Failed renaming budget" } },
      )
    }
  }

  private fun syncStatus(hasServer: Boolean, state: SyncState): SidebarSyncStatus =
    if (!hasServer) {
      LocalOnly
    } else {
      when (state) {
        is SyncState.Error -> SyncError
        Syncing -> Syncing
        Inactive -> Synced
      }
    }

  // Like savePrefs in packages/loot-core/src/server/prefs.ts
  private fun budgetNameChange(name: String) =
    localChange(
      dataset = "prefs",
      row = BudgetName.name,
      column = "value",
      value = name,
    )
}
