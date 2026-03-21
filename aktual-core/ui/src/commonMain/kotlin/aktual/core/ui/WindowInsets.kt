package aktual.core.ui

import aktual.core.theme.LocalTheme
import aktual.core.theme.Theme
import alakazam.compose.VerticalSpacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun SetStatusBarColors(theme: Theme = LocalTheme.current) =
  SetStatusBarColors(statusBarColor = theme.pageBackground, navigationBarColor = Color.Transparent)

@Composable internal expect fun SetStatusBarColors(statusBarColor: Color, navigationBarColor: Color)

// space to block out the bottom navigation bar, so we don't need to adjust layouts to account for
// it
@Composable
fun BottomNavBarSpacing(modifier: Modifier = Modifier) =
  VerticalSpacer(
    modifier = modifier,
    height = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(),
  )
