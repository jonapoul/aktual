package actual.settings.ui

import actual.settings.vm.PreferenceValue
import androidx.compose.runtime.Immutable

@Immutable
internal sealed interface SettingsAction {
  data object NavBack : SettingsAction
  data class PreferenceChange(val value: PreferenceValue) : SettingsAction
}
