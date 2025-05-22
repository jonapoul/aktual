package actual.settings.vm

import actual.core.model.ColorSchemeType
import androidx.compose.runtime.Immutable
import dev.drewhamilton.poko.Poko

@Immutable
sealed interface PreferenceValue {
  @Poko class Theme(val type: ColorSchemeType) : PreferenceValue
}
