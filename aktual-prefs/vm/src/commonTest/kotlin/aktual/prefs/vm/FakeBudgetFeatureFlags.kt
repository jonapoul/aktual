package aktual.prefs.vm

import aktual.budget.model.SyncedPrefKey.FeatureFlag
import aktual.prefs.BudgetFeatureFlags
import aktual.prefs.Preference

internal class FakeBudgetFeatureFlags : BudgetFeatureFlags {
  private val prefs = FeatureFlag.entries.associateWith { InMemoryPreference(false) }

  override fun get(flag: FeatureFlag): Preference<Boolean> = prefs.getValue(flag)
}
