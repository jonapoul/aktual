package aktual.app.nav

import aktual.budget.model.NumberFormat
import aktual.core.model.ColorSchemeType
import aktual.core.model.LoginToken
import aktual.core.ui.AktualTheme
import aktual.core.ui.BottomBarState
import aktual.core.ui.BottomNavBarSpacing
import aktual.core.ui.WithCompositionLocals
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController

@Composable
fun AktualAppContent(
  navController: NavHostController,
  isPrivacyEnabled: Boolean,
  numberFormat: NumberFormat,
  hideFraction: Boolean,
  colorSchemeType: ColorSchemeType,
  isServerUrlSet: Boolean,
  loginToken: LoginToken?,
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
        loginToken = loginToken,
        bottomBarState = bottomBarState,
      )
    }
  }
}

@Composable
private fun AktualAppLayout(
  navController: NavHostController,
  isServerUrlSet: Boolean,
  loginToken: LoginToken?,
  bottomBarState: BottomBarState,
  modifier: Modifier = Modifier,
) = Box(
  modifier = modifier,
  contentAlignment = Alignment.BottomCenter,
) {
  AktualNavHost(
    modifier = Modifier.fillMaxWidth(),
    nav = navController,
    isServerUrlSet = isServerUrlSet,
    loginToken = loginToken,
  )

  if (bottomBarState is BottomBarState.Visible) {
    Column {
      BottomStatusBar(
        modifier = Modifier.wrapContentHeight(),
        state = bottomBarState,
      )
      BottomNavBarSpacing()
    }
  }
}
