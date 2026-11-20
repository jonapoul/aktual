package aktual.test

import aktual.budget.model.SyncedPrefKey.FeatureFlag
import aktual.prefs.BudgetFeatureFlags
import aktual.prefs.Preference

class FakeBudgetFeatureFlags(enabled: Set<FeatureFlag> = emptySet()) : BudgetFeatureFlags {
  private val prefs = FeatureFlag.entries.associateWith { InMemoryPreference(it in enabled) }

  override fun get(flag: FeatureFlag): Preference<Boolean> = prefs.getValue(flag)

  override suspend fun contains(flag: FeatureFlag): Boolean = get(flag).get()
}
