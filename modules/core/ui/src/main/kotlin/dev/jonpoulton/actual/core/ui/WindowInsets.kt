package dev.jonpoulton.actual.core.ui

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun SetStatusBarColors(
  statusBarColor: ActualColorScheme.() -> Color = { mobileHeaderBackground },
  navigationBarColor: ActualColorScheme.() -> Color = { mobileNavBackground },
  darkTheme: Boolean = isSystemInDarkTheme(),
) {
  val view = LocalView.current
  val colorScheme = LocalActualColorScheme.current

  if (!view.isInEditMode) {
    SideEffect {
      val window = (view.context as Activity).window
      window.navigationBarColor = colorScheme.navigationBarColor().toArgb()
      window.statusBarColor = colorScheme.statusBarColor().toArgb()
      WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
    }
  }
}

