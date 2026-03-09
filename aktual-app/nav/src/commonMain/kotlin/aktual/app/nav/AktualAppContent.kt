package aktual.app.nav

import aktual.core.ui.AktualTheme
import aktual.core.ui.BottomBarState
import aktual.core.ui.BottomNavBarSpacing
import aktual.core.ui.LocalBottomStatusBarHeight
import aktual.core.ui.WithCompositionLocals
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController

@Composable
fun AktualAppContent(
  navController: NavHostController,
  viewModel: RootViewModel,
  modifier: Modifier = Modifier,
) {
  val theme by viewModel.theme(isSystemInDarkTheme()).collectAsStateWithLifecycle(null)
  val bottomBarState by viewModel.bottomBarState.collectAsStateWithLifecycle()
  val numberFormat by viewModel.numberFormat.collectAsStateWithLifecycle()
  val hideFraction by viewModel.hideFraction.collectAsStateWithLifecycle()
  val isPrivacyEnabled by viewModel.isPrivacyEnabled.collectAsStateWithLifecycle()
  val currency by viewModel.currency.collectAsStateWithLifecycle()
  val currencySymbolPosition by viewModel.currencySymbolPosition.collectAsStateWithLifecycle()
  val currencySpaceBetweenAmountAndSymbol by
    viewModel.currencySpaceBetweenAmountAndSymbol.collectAsStateWithLifecycle()

  WithCompositionLocals(
    isPrivacyEnabled = isPrivacyEnabled,
    format = numberFormat,
    hideFraction = hideFraction,
    currency = currency,
    currencyPosition = currencySymbolPosition,
    addCurrencySpace = currencySpaceBetweenAmountAndSymbol,
  ) {
    theme?.let { t ->
      AktualTheme(t) {
        Box(modifier = modifier, contentAlignment = Alignment.BottomCenter) {
          var bottomStatusBarHeight by remember { mutableStateOf(0.dp) }

          CompositionLocalProvider(LocalBottomStatusBarHeight provides bottomStatusBarHeight) {
            AktualNavHost(
              modifier = Modifier.fillMaxWidth(),
              nav = navController,
              isServerUrlSet = viewModel.isServerUrlSet,
              token = viewModel.token,
            )
          }

          val bbs = bottomBarState
          if (bbs is BottomBarState.Visible) {
            Column {
              BottomStatusBar(
                modifier = Modifier.wrapContentHeight(),
                state = bbs,
                onMeasureHeight = { bottomStatusBarHeight = it },
              )
              BottomNavBarSpacing()
            }
          }
        }
      }
    }
  }
}
