package actual.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
expect fun SetStatusBarColors(
  theme: Theme = LocalTheme.current,
  statusBarColor: Color = theme.mobileHeaderBackground,
  navigationBarColor: Color = theme.pageBackground,
)
