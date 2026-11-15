package aktual.budget.transactions.domain

import aktual.budget.db.dao.DatabaseTables.PREFERENCES
import aktual.budget.db.dao.PreferencesDao
import aktual.budget.model.SyncedPrefKey
import aktual.budget.model.localChange
import aktual.budget.utils.BudgetSync
import aktual.di.BudgetScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Inject
@ContributesBinding(BudgetScope::class)
class AccountViewPrefsImpl
internal constructor(
  private val preferences: PreferencesDao,
  private val budgetSync: BudgetSync,
) : AccountViewPrefs {
  override fun observe(key: SyncedPrefKey.PerAccount): Flow<Boolean> =
    preferences.observe(key).map { it == "true" }

  override suspend fun set(key: SyncedPrefKey.PerAccount, enabled: Boolean) =
    budgetSync(
      localChange(PREFERENCES, key.key, column = "value", enabled.toString()),
    )
}
