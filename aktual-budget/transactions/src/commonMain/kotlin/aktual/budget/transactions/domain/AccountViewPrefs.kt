package aktual.budget.transactions.domain

import aktual.budget.model.AccountId
import aktual.budget.model.SyncedPrefKey
import kotlinx.coroutines.flow.Flow

// An account's transaction list options, kept in the synced prefs upstream's account screens use.
// Each is off unless it says 'true', as upstream reads them
interface AccountViewPrefs {
  fun observe(key: SyncedPrefKey.PerAccount): Flow<Boolean>

  suspend fun set(key: SyncedPrefKey.PerAccount, enabled: Boolean)

  // Upstream's balance column: from the account's table columns, else the budget's, else the older
  // show-balances pref
  fun observeRunningBalance(account: AccountId): Flow<Boolean>

  suspend fun setRunningBalance(account: AccountId, enabled: Boolean)
}
