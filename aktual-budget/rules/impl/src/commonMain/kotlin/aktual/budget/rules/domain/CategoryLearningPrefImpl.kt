package aktual.budget.rules.domain

import aktual.budget.db.dao.DatabaseTables.PREFERENCES
import aktual.budget.db.dao.PreferencesDao
import aktual.budget.model.SyncedPrefKey.Global.LearnCategories
import aktual.budget.model.localChange
import aktual.budget.utils.BudgetSync
import aktual.di.BudgetScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Inject
@ContributesBinding(BudgetScope::class)
class CategoryLearningPrefImpl(
  private val preferences: PreferencesDao,
  private val budgetSync: BudgetSync,
) : CategoryLearningPref {
  override fun observe(): Flow<Boolean> = preferences.observe(LearnCategories).map(::isEnabled)

  override suspend fun isEnabled(): Boolean = isEnabled(preferences[LearnCategories])

  override suspend fun set(enabled: Boolean) {
    budgetSync(
      localChange(PREFERENCES, LearnCategories.key, column = "value", enabled.toString()),
    )
  }
}

private fun isEnabled(value: String?): Boolean = value == null || value == "true"
