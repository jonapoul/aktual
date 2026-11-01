package aktual.prefs.vm.systemui

import aktual.budget.model.BarEffect
import aktual.budget.model.BarGlassStyle
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
  val glassStyle: ListPreference<BarGlassStyle>,
  val glassCornerRadiusDp: SliderPreference,
  val hidePreviewInAppSwitcher: BooleanPreference,
) {
  // Dialog blur uses the blur radius and alpha too
  val showBlurGroup: Boolean
    get() = appBarEffect.value == Blur || hazeDialogs.value

  val showGlassGroup: Boolean
    get() = appBarEffect.value == Glass
}

fun HazeRadiusPreference(value: Float, onChange: (Float) -> Unit = {}): SliderPreference =
  SliderPreference(value = value, range = 0f..20f, onChange = onChange)

fun HazeAlphaPreference(value: Float, onChange: (Float) -> Unit = {}): SliderPreference =
  SliderPreference(value = value, range = 0f..1f, onChange = onChange)

fun GlassCornerRadiusPreference(value: Float, onChange: (Float) -> Unit = {}): SliderPreference =
  SliderPreference(value = value, range = 0f..32f, onChange = onChange)
