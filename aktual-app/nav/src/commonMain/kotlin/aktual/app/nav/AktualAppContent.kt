package aktual.app.nav

import aktual.core.theme.LocalTheme
import aktual.core.theme.Theme
import aktual.core.theme.isLight
import aktual.core.ui.AktualTheme
import aktual.core.ui.BottomBarState
import aktual.core.ui.BottomNavBarSpacing
import aktual.core.ui.LocalBottomStatusBarHeight
import aktual.core.ui.WithCompositionLocals
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

@Composable
fun rememberBackStack(viewModel: RootViewModel): SnapshotStateList<NavKey> = remember {
  mutableStateListOf(
    when {
      viewModel.token != null && viewModel.isServerUrlSet -> ListBudgetsNavRoute(viewModel.token)
      viewModel.isServerUrlSet -> LoginNavRoute
      else -> ServerUrlNavRoute
    }
  )
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
    AktualTheme(theme ?: return@WithCompositionLocals) {
      Box(modifier = modifier, contentAlignment = Alignment.BottomCenter) {
        var bottomStatusBarHeight by remember { mutableStateOf(0.dp) }
        val hazeState = rememberHazeState()

        CompositionLocalProvider(LocalBottomStatusBarHeight provides bottomStatusBarHeight) {
          AktualNavHost(
            modifier =
              Modifier.fillMaxSize()
                .consumeWindowInsets(WindowInsets.navigationBars)
                .hazeSource(state = hazeState),
            backStack = backStack,
          )
        }

        val theme = LocalTheme.current
        Column(modifier = Modifier.hazeEffect(state = hazeState, style = theme.hazeStyle())) {
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

private const val HAZE_ALPHA = 0.5f
private val HAZE_RADIUS = 5.dp

private fun Theme.hazeStyle(
  tintAlpha: Float = if (isLight()) 1f - HAZE_ALPHA else HAZE_ALPHA
): HazeStyle =
  HazeStyle(
    blurRadius = HAZE_RADIUS,
    backgroundColor = cardBackground,
    tint = HazeTint(cardBackground.copy(alpha = tintAlpha)),
  )
