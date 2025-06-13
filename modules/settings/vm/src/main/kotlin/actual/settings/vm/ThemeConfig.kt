package actual.settings.vm

import actual.core.model.DarkColorSchemeType
import actual.core.model.RegularColorSchemeType
import androidx.compose.runtime.Immutable

@Immutable
data class ThemeConfig(
  val regular: RegularColorSchemeType,
  val dark: DarkColorSchemeType,
)
