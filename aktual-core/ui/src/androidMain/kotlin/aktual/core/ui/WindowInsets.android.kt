package aktual.core.ui

import aktual.core.theme.isLight
import android.app.Activity
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * [android.view.Window.setNavigationBarColor] and [android.view.Window.setStatusBarColor] are
 * deprecated, and have no effect in Android 15
 */
@Suppress("DEPRECATION")
@Composable
actual fun SetStatusBarColors(statusBarColor: Color, navigationBarColor: Color) {
  val view = LocalView.current

  if (!view.isInEditMode) {
    LaunchedEffect(statusBarColor, navigationBarColor) {
      val window = (view.context as Activity).window
      window.statusBarColor = statusBarColor.toArgb()

      // enableEdgeToEdge with auto style sets isNavigationBarContrastEnforced = true in light mode,
      // which causes the system to draw a scrim over transparent nav bars. Disable it so our haze
      // effect is visible.
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        window.isNavigationBarContrastEnforced = false
      }

      WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars =
        statusBarColor.isLight()
    }
  }
}
