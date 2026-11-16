package aktual.prefs.vm.dialogs

import aktual.prefs.vm.BooleanPreference
import aktual.prefs.vm.SliderPreference
import androidx.compose.runtime.Immutable

@Immutable
data class DialogSettingsState(
  val blurDialogs: BooleanPreference,
  val blurRadius: SliderPreference,
  val blurAlpha: SliderPreference,
)
