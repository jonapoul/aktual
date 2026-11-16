package aktual.prefs.vm.systemui

import aktual.prefs.vm.BooleanPreference
import androidx.compose.runtime.Immutable

@Immutable data class SystemUiSettingsState(val showStatusBar: BooleanPreference)
