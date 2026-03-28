package aktual.app.nav

import aktual.core.ui.AktualTheme
import aktual.core.ui.BottomBarState
import aktual.core.ui.BottomNavBarSpacing
import aktual.core.ui.DialogBlurOverlay
import aktual.core.ui.DialogBlurState
import aktual.core.ui.LocalBottomStatusBarHeight
import aktual.core.ui.WithCompositionLocals
import aktual.core.ui.blurred
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

@Composable
fun rememberBackStack(viewModel: RootViewModel): SnapshotStateList<NavKey>? {
  // don't collectAsStateWithLifecycle, we don't have a lifecycle yet (on desktop)
  val initialRoute by viewModel.initialRoute.collectAsState()
  val route = initialRoute ?: return null
  return remember(viewModel) { viewModel.getOrCreateBackStack(route) }
}

@Composable
fun AktualAppContent(
  viewModel: RootViewModel,
  backStack: SnapshotStateList<NavKey>,
  modifier: Modifier = Modifier,
) {
  LaunchedEffect(viewModel) {
    viewModel.tokenExpired.collect {
      backStack.clear()
      backStack.add(LoginNavRoute)
    }
  }

  val isSystemDark = isSystemInDarkTheme()
  LaunchedEffect(isSystemDark) { viewModel.updateSystemDarkTheme(isSystemDark) }

  val theme by viewModel.theme.collectAsStateWithLifecycle()
  val bottomBarState by viewModel.bottomBarState.collectAsStateWithLifecycle()
  val formatConfig by viewModel.formatConfig.collectAsStateWithLifecycle()
  val blurConfig by viewModel.blurConfig.collectAsStateWithLifecycle()

  val hazeState = rememberHazeState()

  WithCompositionLocals(
    isPrivacyEnabled = formatConfig.isPrivacyEnabled,
    format = formatConfig.numberFormat,
    hideFraction = formatConfig.hideFraction,
    currency = formatConfig.currency,
    currencyPosition = formatConfig.symbolPosition,
    addCurrencySpace = formatConfig.spaceBetweenAmountAndSymbol,
    hazeState = hazeState,
    blurConfig = blurConfig,
    dialogBlurState = remember { DialogBlurState() },
  ) {
    AktualTheme(theme) {
      Box(modifier = modifier, contentAlignment = Alignment.BottomCenter) {
        var bottomStatusBarHeight by remember { mutableStateOf(0.dp) }

        CompositionLocalProvider(LocalBottomStatusBarHeight provides bottomStatusBarHeight) {
          AktualNavHost(
            modifier =
              Modifier.fillMaxSize()
                .consumeWindowInsets(WindowInsets.navigationBars)
                .hazeSource(hazeState),
            backStack = backStack,
            contributors = viewModel.navEntryContributors,
          )
        }

        DialogBlurOverlay()

        Column(modifier = Modifier.blurred(orElse = { pageBackground })) {
          val bbs = bottomBarState
          if (bbs is BottomBarState.Visible) {
            BottomStatusBar(
              modifier = Modifier.wrapContentHeight(),
              state = bbs,
              onMeasureHeight = { bottomStatusBarHeight = it },
            )
          }
          BottomNavBarSpacing()
        }
      }
    }
  }
}
