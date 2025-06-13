package actual.core.ui

import actual.core.model.ColorSchemeType
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color

@Composable
fun ActualTheme(
  type: ColorSchemeType,
  content: @Composable () -> Unit,
) {
  val theme = remember(type) {
    when (type) {
      ColorSchemeType.Light -> LightTheme()
      ColorSchemeType.Dark -> DarkTheme()
      ColorSchemeType.Midnight -> MidnightTheme()
    }
  }

  CompositionLocalProvider(
    LocalTheme provides theme,
    LocalColorSchemeType provides type,
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
