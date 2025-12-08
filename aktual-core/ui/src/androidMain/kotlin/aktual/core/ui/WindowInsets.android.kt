package aktual.core.ui

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * [android.view.Window.setNavigationBarColor] and [android.view.Window.setStatusBarColor] are deprecated, and have no
 * effect in Android 15
 */
@Suppress("DEPRECATION")
@Composable
actual fun SetStatusBarColors(
  theme: Theme,
  statusBarColor: Color,
  navigationBarColor: Color,
) {
  val view = LocalView.current

  if (!view.isInEditMode) {
    LaunchedEffect(theme) {
      val window = (view.context as Activity).window
      window.navigationBarColor = navigationBarColor.toArgb()
      window.statusBarColor = statusBarColor.toArgb()
      WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = theme.isLight()
    }
  }
}
