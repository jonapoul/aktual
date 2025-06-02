package actual.settings.vm

import actual.core.model.ColorSchemeType
import androidx.compose.runtime.Immutable

@Immutable
sealed interface PreferenceValue {
  data class Theme(val type: ColorSchemeType) : PreferenceValue
  data class ShowBottomBar(val show: Boolean) : PreferenceValue
}
