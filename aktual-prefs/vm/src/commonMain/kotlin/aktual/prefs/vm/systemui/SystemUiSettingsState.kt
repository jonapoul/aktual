package aktual.prefs.vm.systemui

import aktual.budget.model.BarEffect
import aktual.budget.model.BarGlassStyle
import aktual.prefs.vm.BooleanPreference
import aktual.prefs.vm.ListPreference
import aktual.prefs.vm.SliderPreference
import androidx.compose.runtime.Immutable

@Immutable
data class SystemUiSettingsState(
  val hidePreviewInAppSwitcher: BooleanPreference,
  val showStatusBar: BooleanPreference,
  val barEffect: ListPreference<BarEffect>,
  val barBlurRadius: SliderPreference,
  val barBlurAlpha: SliderPreference,
  val glassStyle: ListPreference<BarGlassStyle>,
  val glassCornerRadiusDp: SliderPreference,
  val blurDialogs: BooleanPreference,
  val dialogBlurRadius: SliderPreference,
  val dialogBlurAlpha: SliderPreference,
)

fun BlurRadiusPreference(
  value: Float,
  enabled: Boolean = true,
  onChange: (Float) -> Unit = {},
): SliderPreference =
  SliderPreference(value = value, range = 0f..20f, enabled = enabled, onChange = onChange)

fun BlurAlphaPreference(
  value: Float,
  enabled: Boolean = true,
  onChange: (Float) -> Unit = {},
): SliderPreference =
  SliderPreference(value = value, range = 0f..1f, enabled = enabled, onChange = onChange)

fun GlassCornerRadiusPreference(
  value: Float,
  enabled: Boolean = true,
  onChange: (Float) -> Unit = {},
): SliderPreference =
  SliderPreference(value = value, range = 0f..32f, enabled = enabled, onChange = onChange)
