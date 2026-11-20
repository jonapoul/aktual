package aktual.prefs

import aktual.budget.model.SyncedPrefKey.FeatureFlag
import androidx.compose.runtime.Stable

// Upstream's experimental feature toggles, synced with the open budget
@Stable
interface BudgetFeatureFlags {
  operator fun get(flag: FeatureFlag): Preference<Boolean>

  suspend operator fun contains(flag: FeatureFlag): Boolean
}
