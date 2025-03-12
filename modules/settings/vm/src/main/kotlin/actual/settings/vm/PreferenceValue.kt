package actual.settings.vm

import actual.core.colorscheme.ColorSchemeType
import androidx.compose.runtime.Immutable

@Immutable
sealed interface PreferenceValue {
  data class Theme(val type: ColorSchemeType) : PreferenceValue
}
