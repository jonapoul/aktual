package actual.core.ui

import actual.core.model.ColorSchemeType
import actual.core.model.ColorSchemeType.Dark
import actual.core.model.ColorSchemeType.Light
import actual.core.model.ColorSchemeType.Midnight
import actual.core.model.ColorSchemeType.System
import actual.core.model.DarkColorSchemeType
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color

@Composable
fun ActualTheme(
  regularScheme: ColorSchemeType,
  darkScheme: DarkColorSchemeType = Dark,
  content: @Composable () -> Unit,
) {
  val systemDarkTheme = isSystemInDarkTheme()

  val theme = remember(regularScheme, darkScheme, systemDarkTheme) {
    when (regularScheme) {
      System -> when (darkScheme) {
        Dark -> if (systemDarkTheme) DarkTheme() else LightTheme()
        Midnight -> if (systemDarkTheme) MidnightTheme() else LightTheme()
      }

      Dark -> DarkTheme()
      Light -> LightTheme()
      Midnight -> MidnightTheme()
    }
  }

  CompositionLocalProvider(
    LocalTheme provides theme,
    LocalColorSchemeType provides regularScheme,
    LocalDarkSchemeType provides darkScheme,
  ) {
    SetStatusBarColors(
      theme = theme,
    )

    val materialColorScheme = when (theme) {
      is LightTheme -> lightColorScheme()
      is DarkTheme -> darkColorScheme()
      is MidnightTheme -> darkColorScheme(surface = Color.Black)
    }

    MaterialTheme(
      colorScheme = materialColorScheme,
      typography = typography(theme),
      content = content,
    )
  }
}
