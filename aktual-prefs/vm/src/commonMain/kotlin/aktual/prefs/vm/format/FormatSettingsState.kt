package aktual.prefs.vm.format

import aktual.core.model.DistanceUnit
import aktual.prefs.vm.ListPreference
import androidx.compose.runtime.Immutable

@Immutable data class FormatSettingsState(val distanceUnit: ListPreference<DistanceUnit>)
