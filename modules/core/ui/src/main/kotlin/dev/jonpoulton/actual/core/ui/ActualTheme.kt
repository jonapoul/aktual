package dev.jonpoulton.actual.core.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
fun ActualTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit,
) {
  // TODO: Handle user preferences here
  // TODO: Support midnight colours
  val colorScheme = when {
    darkTheme -> DarkColorScheme
    else -> LightColorScheme
  }

  CompositionLocalProvider(
    LocalActualColorScheme provides colorScheme,
  ) {
    SetStatusBarColors(
      darkTheme = darkTheme,
    )

    MaterialTheme(
      colorScheme = if (darkTheme) darkColorScheme() else lightColorScheme(),
      typography = ActualTypography,
      content = content,
    )
  }
}
