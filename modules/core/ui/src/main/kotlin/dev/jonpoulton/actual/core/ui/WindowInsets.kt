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
  colors: ActualColorScheme = LocalActualColorScheme.current,
  statusBarColor: Color = colors.mobileHeaderBackground,
  navigationBarColor: Color = colors.pageBackground,
  darkTheme: Boolean = isSystemInDarkTheme(),
) {
  val view = LocalView.current

  if (!view.isInEditMode) {
    SideEffect {
      val window = (view.context as Activity).window
      window.navigationBarColor = navigationBarColor.toArgb()
      window.statusBarColor = statusBarColor.toArgb()
      WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
    }
  }
}
