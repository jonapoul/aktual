package aktual.budget.navrail.vm

import aktual.api.client.BudgetSyncApi
import aktual.budget.BudgetLocalPreferences
import aktual.budget.BudgetSyncController
import aktual.budget.banksync.domain.NewTransactions
import aktual.budget.db.dao.AccountDao
import aktual.budget.model.DbMetadataKey.Companion.BudgetName
import aktual.budget.model.DbMetadataKey.Companion.CloudFileId
import aktual.budget.model.localChange
import aktual.budget.schedules.domain.UnpostedSchedules
import aktual.core.model.BudgetServer
import aktual.core.nav.BudgetNavEntryContributor
import aktual.core.runCatchingCancellable
import aktual.di.BudgetScope
import aktual.di.ViewModelContext
import aktual.di.newScope
import aktual.prefs.AppPreferences
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
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import logcat.logcat

@Stable
@ViewModelKey
@ContributesIntoMap(BudgetScope::class)
class BudgetNavRailViewModel
internal constructor(
  contributors: Set<BudgetNavEntryContributor>,
  private val localPreferences: BudgetLocalPreferences,
  private val server: BudgetServer,
  private val syncApi: BudgetSyncApi,
  private val sync: BudgetSyncController,
  accountDao: AccountDao,
  newTransactions: NewTransactions,
  private val appPreferences: AppPreferences,
  private val unpostedSchedules: UnpostedSchedules,
  viewModelContext: ViewModelContext,
) : ViewModel(viewModelContext.newScope()) {
  val budgetNavEntryContributors: ImmutableSet<BudgetNavEntryContributor> =
    contributors.toImmutableSet()

  val headerState: StateFlow<DrawerHeaderState> =
    viewModelScope.launchMolecule(Immediate) {
      val budgetNameFlow = remember { localPreferences.observe(BudgetName) }
      val budgetName by budgetNameFlow.collectAsState(initial = localPreferences[BudgetName])
      val serverHost = (server as? Remote)?.url?.baseUrl
      DrawerHeaderState(budgetName = budgetName, serverHost = serverHost)
    }

  val accounts: StateFlow<DrawerAccounts> =
    combine(accountDao.observeAllWithBalances(), newTransactions.counts) { rows, counts ->
        rows.toDrawerAccounts(counts)
      }
      .catch { e -> logcat.e(e) { "Failed loading accounts" } }
      .stateIn(viewModelScope, Eagerly, initialValue = DrawerAccounts())

  val hasUnpostedSchedules: StateFlow<Boolean> = unpostedSchedules.hasUnposted

  fun postUnpostedSchedules() = unpostedSchedules.post()

  fun dismissUnpostedSchedules() = unpostedSchedules.dismiss()

  fun setPrivacyMode(isEnabled: Boolean) {
    viewModelScope.launch { appPreferences.isPrivacyEnabled.set(isEnabled) }
  }

  fun rename(name: String) {
    viewModelScope.launch {
      runCatchingCancellable {
        // The server keeps its own copy of the name for the budget list, see saveMetadataPrefs in
        // packages/loot-core/src/server/preferences/app.ts
        val cloudFileId = localPreferences[CloudFileId]
        if (server is Remote && cloudFileId != null) {
          syncApi.renameBudget(cloudFileId, name)
        }
        localPreferences.update { metadata -> metadata.set(BudgetName, name) }
        sync.syncChanges(budgetNameChange(name))
      }
        .onFailure { e ->
          logcat.e(e) { "Failed renaming budget" }
        }
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
