package aktual.core.ui

import aktual.core.theme.LocalTheme
import aktual.core.theme.Theme
import alakazam.compose.HorizontalSpacer
import alakazam.compose.VerticalSpacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
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

@Composable
fun SideSpacing(
  modifier: Modifier = Modifier,
  width: Dp = LocalBottomSpacing.current,
  direction: LayoutDirection = LocalLayoutDirection.current,
) {
  val padding = WindowInsets.navigationBars.asPaddingValues()
  val start = padding.calculateStartPadding(direction)
  val end = padding.calculateEndPadding(direction)

  val navBar = maxOf(start, end)
  HorizontalSpacer(modifier = modifier, width = navBar + width)
}
