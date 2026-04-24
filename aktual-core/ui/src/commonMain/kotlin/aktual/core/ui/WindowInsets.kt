package aktual.core.ui

import aktual.core.theme.LocalTheme
import aktual.core.theme.Theme
import alakazam.compose.VerticalSpacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun SetStatusBarColors(theme: Theme = LocalTheme.current) =
  SetStatusBarColors(statusBarColor = theme.pageBackground, navigationBarColor = Color.Transparent)

@Composable internal expect fun SetStatusBarColors(statusBarColor: Color, navigationBarColor: Color)

@Composable
fun bottomNavBarPadding(): Dp =
  WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

val LocalBottomSpacing = compositionLocalOf { 0.dp }

@Composable
fun BottomSpacing(modifier: Modifier = Modifier, height: Dp = LocalBottomSpacing.current) {
  val navBar = bottomNavBarPadding()
  VerticalSpacer(modifier = modifier, height = navBar + height)
}
