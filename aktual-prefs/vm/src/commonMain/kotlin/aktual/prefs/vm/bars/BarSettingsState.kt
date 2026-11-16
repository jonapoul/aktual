package aktual.prefs.vm.bars

import aktual.budget.model.BarEffect
import aktual.budget.model.BarGlassStyle
import aktual.prefs.vm.ListPreference
import aktual.prefs.vm.SliderPreference
import androidx.compose.runtime.Immutable

@Immutable
data class BarSettingsState(
  val barEffect: ListPreference<BarEffect>,
  val blurRadius: SliderPreference,
  val blurAlpha: SliderPreference,
  val glassStyle: ListPreference<BarGlassStyle>,
  val glassCornerRadiusDp: SliderPreference,
)

fun GlassCornerRadiusPreference(
  value: Float,
  enabled: Boolean = true,
  onChange: (Float) -> Unit = {},
): SliderPreference =
  SliderPreference(value = value, range = 0f..32f, enabled = enabled, onChange = onChange)
