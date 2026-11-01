package aktual.prefs.vm.security

import aktual.prefs.AppLockTimeout
import aktual.prefs.vm.BooleanPreference
import aktual.prefs.vm.ListPreference
import androidx.compose.runtime.Immutable

@Immutable
data class SecuritySettingsState(
  val appLockEnabled: BooleanPreference,
  val appLockTimeout: ListPreference<AppLockTimeout>,
  val hidePreviewInAppSwitcher: BooleanPreference,
  val isScreenLockAvailable: Boolean,
)
