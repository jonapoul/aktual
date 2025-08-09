package actual.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color

// No-op on desktop
@Stable
@Composable
actual fun SetStatusBarColors(
  theme: Theme,
  statusBarColor: Color,
  navigationBarColor: Color,
) = Unit
