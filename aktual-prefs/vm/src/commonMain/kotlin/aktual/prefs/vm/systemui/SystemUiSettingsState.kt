package aktual.prefs.vm.systemui

import aktual.budget.model.BarEffect
import aktual.prefs.vm.BooleanPreference
import aktual.prefs.vm.ListPreference
import aktual.prefs.vm.SliderPreference
import androidx.compose.runtime.Immutable

@Immutable
data class SystemUiSettingsState(
  val showStatusBar: BooleanPreference,
  val appBarEffect: ListPreference<BarEffect>,
  val hazeDialogs: BooleanPreference,
  val hazeRadiusDp: SliderPreference,
  val hazeAlpha: SliderPreference,
  val hidePreviewInAppSwitcher: BooleanPreference,
)

fun HazeRadiusPreference(
  value: Float,
  enabled: Boolean = true,
  onChange: (Float) -> Unit = {},
): SliderPreference =
  SliderPreference(value = value, range = 0f..20f, enabled = enabled, onChange = onChange)

fun HazeAlphaPreference(
  value: Float,
  enabled: Boolean = true,
  onChange: (Float) -> Unit = {},
): SliderPreference =
  SliderPreference(value = value, range = 0f..1f, enabled = enabled, onChange = onChange)
