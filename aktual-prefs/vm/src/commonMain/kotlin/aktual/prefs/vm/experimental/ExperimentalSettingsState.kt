package aktual.prefs.vm.experimental

import aktual.budget.model.SyncedPrefKey.FeatureFlag
import aktual.prefs.vm.BooleanPreference
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable data class ExperimentalSettingsState(val features: ImmutableList<ExperimentalFeature>)

// Flags Aktual doesn't support yet show their synced value but can't be changed
@Immutable
data class ExperimentalFeature(
  val flag: FeatureFlag,
  val preference: BooleanPreference,
  val isSupported: Boolean,
)
