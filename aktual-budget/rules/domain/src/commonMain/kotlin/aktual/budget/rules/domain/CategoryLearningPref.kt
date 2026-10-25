package aktual.budget.rules.domain

import aktual.budget.BudgetSyncController
import aktual.budget.db.dao.DatabaseTables.PREFERENCES
import aktual.budget.db.dao.PreferencesDao
import aktual.budget.model.SyncedPrefKey.Global.LearnCategories
import aktual.budget.model.localChange
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// The global learn-categories synced pref, read as upstream's String(value ?? 'true') === 'true'
@Inject
class CategoryLearningPref(
  private val preferences: PreferencesDao,
  private val syncController: BudgetSyncController,
) {
  fun observe(): Flow<Boolean> = preferences.observe(LearnCategories).map(::isEnabled)

  suspend fun isEnabled(): Boolean = isEnabled(preferences[LearnCategories])

  suspend fun set(enabled: Boolean) =
    syncController.syncChanges(
      localChange(PREFERENCES, LearnCategories.key, column = "value", enabled.toString()),
    )
}

private fun isEnabled(value: String?): Boolean = value == null || value == "true"
