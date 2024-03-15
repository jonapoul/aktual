package dev.jonpoulton.actual.core.ui

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

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

  val view = LocalView.current
  if (!view.isInEditMode) {
    SideEffect {
      val window = (view.context as Activity).window
      window.statusBarColor = colorScheme.mobileHeaderBackground.toArgb()
      WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
    }
  }

  CompositionLocalProvider(
    LocalActualColorScheme provides colorScheme,
  ) {
    MaterialTheme(
      colorScheme = if (darkTheme) darkColorScheme() else lightColorScheme(),
      typography = ActualTypography,
      content = content,
    )
  }
}
