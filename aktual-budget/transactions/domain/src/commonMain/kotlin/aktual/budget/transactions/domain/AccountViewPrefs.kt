package aktual.budget.transactions.domain

import aktual.budget.BudgetSyncController
import aktual.budget.db.dao.DatabaseTables.PREFERENCES
import aktual.budget.db.dao.PreferencesDao
import aktual.budget.model.SyncedPrefKey
import aktual.budget.model.localChange
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// An account's transaction list options, kept in the synced prefs upstream's account screens use.
// Each is off unless it says 'true', as upstream reads them
@Inject
class AccountViewPrefs
internal constructor(
  private val preferences: PreferencesDao,
  private val syncController: BudgetSyncController,
) {
  fun observe(key: SyncedPrefKey.PerAccount): Flow<Boolean> =
    preferences.observe(key).map { it == "true" }

  suspend fun set(key: SyncedPrefKey.PerAccount, enabled: Boolean) =
    syncController.syncChanges(
      localChange(PREFERENCES, key.key, column = "value", enabled.toString()),
    )
}
