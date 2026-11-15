package aktual.budget.transactions.domain

import aktual.budget.model.SyncedPrefKey
import kotlinx.coroutines.flow.Flow

// An account's transaction list options, kept in the synced prefs upstream's account screens use.
// Each is off unless it says 'true', as upstream reads them
interface AccountViewPrefs {
  fun observe(key: SyncedPrefKey.PerAccount): Flow<Boolean>

  suspend fun set(key: SyncedPrefKey.PerAccount, enabled: Boolean)
}
