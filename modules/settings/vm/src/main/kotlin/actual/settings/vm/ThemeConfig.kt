package actual.settings.vm

import actual.core.model.ColorSchemeType
import actual.core.model.DarkColorSchemeType
import androidx.compose.runtime.Immutable

@Immutable
data class ThemeConfig(
  val theme: ColorSchemeType,
  val darkTheme: DarkColorSchemeType,
)
