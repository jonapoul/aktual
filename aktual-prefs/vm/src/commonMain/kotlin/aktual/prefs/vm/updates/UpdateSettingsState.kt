package aktual.prefs.vm.updates

import aktual.prefs.vm.BooleanPreference
import androidx.compose.runtime.Immutable

@Immutable data class UpdateSettingsState(val checkNightlies: BooleanPreference)
