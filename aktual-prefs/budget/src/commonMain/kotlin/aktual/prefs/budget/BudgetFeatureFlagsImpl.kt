package aktual.prefs.budget

import aktual.budget.db.dao.PreferencesDao
import aktual.budget.model.SyncedPrefKey.FeatureFlag
import aktual.budget.utils.BudgetSync
import aktual.di.BudgetScope
import aktual.prefs.BudgetFeatureFlags
import aktual.prefs.Preference
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

@Inject
@ContributesBinding(BudgetScope::class)
class BudgetFeatureFlagsImpl internal constructor(dao: PreferencesDao, budgetSync: BudgetSync) :
  BudgetFeatureFlags {
  private val prefs: Map<FeatureFlag, Preference<Boolean>> =
    FeatureFlag.entries.associateWith { flag ->
      booleanPref(dao, budgetSync, flag, default = false)
    }

  override fun get(flag: FeatureFlag): Preference<Boolean> = prefs.getValue(flag)
}
