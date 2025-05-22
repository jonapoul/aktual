package actual.settings.ui

import actual.settings.vm.PreferenceValue
import androidx.compose.runtime.Immutable
import dev.drewhamilton.poko.Poko

@Immutable
internal sealed interface SettingsAction {
  data object NavBack : SettingsAction
  @Poko class PreferenceChange(val value: PreferenceValue) : SettingsAction
}
