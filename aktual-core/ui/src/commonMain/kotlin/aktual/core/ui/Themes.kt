package aktual.core.ui

import aktual.core.model.ColorSchemeType
import aktual.core.model.DarkColorSchemeType
import aktual.core.model.RegularColorSchemeType
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

val LocalTheme = compositionLocalOf<Theme> { error("CompositionLocal Theme not present") }

@Stable fun Theme.isLight(): Boolean = pageBackground.isLight()

@Stable fun Color.isLight(): Boolean = luminance() > 0.5f

@Composable
@ReadOnlyComposable
fun chooseSchemeType(regular: RegularColorSchemeType, dark: DarkColorSchemeType) =
  when (regular) {
    RegularColorSchemeType.Light -> ColorSchemeType.Light

    RegularColorSchemeType.Dark -> dark.toColorSchemeType()

    RegularColorSchemeType.System ->
      if (isSystemInDarkTheme()) {
        dark.toColorSchemeType()
      } else {
        ColorSchemeType.Light
      }
  }

@Stable
private fun DarkColorSchemeType.toColorSchemeType(): ColorSchemeType =
  when (this) {
    DarkColorSchemeType.Dark -> ColorSchemeType.Dark
    DarkColorSchemeType.Midnight -> ColorSchemeType.Midnight
  }
