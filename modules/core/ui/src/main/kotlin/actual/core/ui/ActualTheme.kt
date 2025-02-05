package actual.core.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

@Composable
fun ActualTheme(
  schemeType: ActualColorSchemeType = ActualColorSchemeType.System,
  content: @Composable () -> Unit,
) {
  val systemDarkTheme = isSystemInDarkTheme()

  val colorScheme = when (schemeType) {
    ActualColorSchemeType.System -> if (systemDarkTheme) DarkTheme() else LightTheme()
    ActualColorSchemeType.Dark -> DarkTheme()
    ActualColorSchemeType.Light -> LightTheme()
    ActualColorSchemeType.Midnight -> MidnightTheme()
  }

  CompositionLocalProvider(
    LocalTheme provides colorScheme,
  ) {
    SetStatusBarColors(
      theme = colorScheme,
      darkTheme = systemDarkTheme,
    )

    val materialColorScheme = when (colorScheme) {
      is LightTheme -> lightColorScheme()
      is DarkTheme -> darkColorScheme()
      is MidnightTheme -> darkColorScheme(surface = Color.Black)
    }

    MaterialTheme(
      colorScheme = materialColorScheme,
      typography = actualTypography(colorScheme),
      content = content,
    )
  }
}
