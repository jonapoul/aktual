package aktual.app.nav

import aktual.budget.model.NumberFormat
import aktual.core.model.ColorSchemeType
import aktual.core.model.Token
import aktual.core.ui.AktualTheme
import aktual.core.ui.BottomBarState
import aktual.core.ui.BottomNavBarSpacing
import aktual.core.ui.LocalBottomStatusBarHeight
import aktual.core.ui.WithCompositionLocals
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun AktualAppContent(
    navController: NavHostController,
    isPrivacyEnabled: Boolean,
    numberFormat: NumberFormat,
    hideFraction: Boolean,
    colorSchemeType: ColorSchemeType,
    isServerUrlSet: Boolean,
    token: Token?,
    bottomBarState: BottomBarState,
) {
  WithCompositionLocals(
      isPrivacyEnabled = isPrivacyEnabled,
      format = numberFormat,
      hideFraction = hideFraction,
  ) {
    AktualTheme(colorSchemeType) {
      AktualAppLayout(
          navController = navController,
          isServerUrlSet = isServerUrlSet,
          token = token,
          bottomBarState = bottomBarState,
      )
    }
  }
}

@Composable
private fun AktualAppLayout(
    navController: NavHostController,
    isServerUrlSet: Boolean,
    token: Token?,
    bottomBarState: BottomBarState,
    modifier: Modifier = Modifier,
) =
    Box(
        modifier = modifier,
        contentAlignment = Alignment.BottomCenter,
    ) {
      var bottomStatusBarHeight by remember { mutableStateOf(0.dp) }

      CompositionLocalProvider(LocalBottomStatusBarHeight provides bottomStatusBarHeight) {
        AktualNavHost(
            modifier = Modifier.fillMaxWidth(),
            nav = navController,
            isServerUrlSet = isServerUrlSet,
            token = token,
        )
      }

      if (bottomBarState is BottomBarState.Visible) {
        Column {
          BottomStatusBar(
              modifier = Modifier.wrapContentHeight(),
              state = bottomBarState,
              onMeasureHeight = { bottomStatusBarHeight = it },
          )
          BottomNavBarSpacing()
        }
      }
    }
