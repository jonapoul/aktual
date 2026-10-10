package aktual.budget.navrail.vm

import aktual.api.client.BudgetSyncApi
import aktual.budget.BudgetLocalPreferences
import aktual.budget.BudgetSyncController
import aktual.budget.db.dao.AccountDao
import aktual.budget.model.DbMetadata
import aktual.budget.model.localChange
import aktual.core.model.BudgetServer
import aktual.core.nav.BudgetNavEntryContributor
import aktual.di.BudgetScope
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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import logcat.logcat

@Stable
@ViewModelKey
@ContributesIntoMap(BudgetScope::class)
class BudgetNavRailViewModel(
  contributors: Set<BudgetNavEntryContributor>,
  private val localPreferences: BudgetLocalPreferences,
  private val server: BudgetServer,
  private val syncApi: BudgetSyncApi,
  private val sync: BudgetSyncController,
  accountDao: AccountDao,
  private val appPreferences: AppPreferences,
) : ViewModel() {
  val budgetNavEntryContributors: ImmutableSet<BudgetNavEntryContributor> =
    contributors.toImmutableSet()

  val headerState: StateFlow<DrawerHeaderState> =
    viewModelScope.launchMolecule(Immediate) {
      val budgetNameFlow = remember { localPreferences.observe(DbMetadata.BudgetName) }
      val budgetName by
        budgetNameFlow.collectAsState(initial = localPreferences[DbMetadata.BudgetName])
      val serverHost = (server as? BudgetServer.Remote)?.url?.baseUrl
      DrawerHeaderState(budgetName = budgetName, serverHost = serverHost)
    }

  val accounts: StateFlow<DrawerAccounts> =
    accountDao
      .observeAllWithBalances()
      .map { rows -> rows.toDrawerAccounts() }
      .catch { e -> logcat.e(e) { "Failed loading accounts" } }
      .stateIn(viewModelScope, Eagerly, initialValue = DrawerAccounts())

  fun setPrivacyMode(isEnabled: Boolean) {
    viewModelScope.launch { appPreferences.isPrivacyEnabled.set(isEnabled) }
  }

  fun rename(name: String) {
    viewModelScope.launch {
      try {
        // The server keeps its own copy of the name for the budget list, see saveMetadataPrefs in
        // packages/loot-core/src/server/preferences/app.ts
        val cloudFileId = localPreferences[DbMetadata.CloudFileId]
        if (server is Remote && cloudFileId != null) {
          syncApi.renameBudget(cloudFileId, name)
        }
        localPreferences.update { metadata -> metadata.set(DbMetadata.BudgetName, name) }
        sync.syncChanges(budgetNameChange(name))
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        logcat.e(e) { "Failed renaming budget" }
      }
    }
  }

  // Like savePrefs in packages/loot-core/src/server/prefs.ts
  private fun budgetNameChange(name: String) =
    localChange(
      dataset = "prefs",
      row = DbMetadata.BudgetName.name,
      column = "value",
      value = name,
    )
}
