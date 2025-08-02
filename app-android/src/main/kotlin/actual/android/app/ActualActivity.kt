package actual.android.app

import actual.account.model.LoginToken
import actual.android.app.nav.ActualNavHost
import actual.app.di.ViewModelGraph
import actual.core.model.ColorSchemeType
import actual.core.model.DarkColorSchemeType
import actual.core.model.RegularColorSchemeType
import actual.core.ui.ActualTheme
import actual.core.ui.LocalViewModelProvidersBuilder
import actual.core.ui.WithCompositionLocals
import alakazam.kotlin.compose.VerticalSpacer
import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding

@Inject
@ActivityKey(ActualActivity::class)
@ContributesIntoMap(AppScope::class, binding<Activity>())
class ActualActivity(vmGraphBuilder: ViewModelGraph.Builder) : ComponentActivity() {
  override val defaultViewModelProviderFactory: ViewModelGraph.Builder = vmGraphBuilder

  private val viewModel by viewModels<ActualActivityViewModel>()

  override fun onDestroy() {
    super.onDestroy()
    viewModel.onDestroy()
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    viewModel.start()

    installSplashScreen()

    val transparent = Color.Transparent.value.toInt()
    enableEdgeToEdge(
      statusBarStyle = SystemBarStyle.auto(transparent, transparent),
      navigationBarStyle = SystemBarStyle.auto(transparent, transparent),
    )

    setContent {
      val regular by viewModel.regularSchemeType.collectAsStateWithLifecycle()
      val darkScheme by viewModel.darkSchemeType.collectAsStateWithLifecycle()
      val bottomBarState by viewModel.bottomBarState.collectAsStateWithLifecycle()
      val numberFormat by viewModel.numberFormat.collectAsStateWithLifecycle()
      val hideFraction by viewModel.hideFraction.collectAsStateWithLifecycle()
      val isPrivacyEnabled by viewModel.isPrivacyEnabled.collectAsStateWithLifecycle()
      val colorSchemeType = chooseSchemeType(regular, darkScheme)

      CompositionLocalProvider(
        LocalViewModelProvidersBuilder provides defaultViewModelProviderFactory,
      ) {
        WithCompositionLocals(
          isPrivacyEnabled = isPrivacyEnabled,
          format = numberFormat,
          hideFractions = hideFraction,
        ) {
          ActualTheme(colorSchemeType) {
            Content(
              isServerUrlSet = viewModel.isServerUrlSet,
              loginToken = viewModel.loginToken,
              bottomBarState = bottomBarState,
            )
          }
        }
      }
    }
  }
}

@ReadOnlyComposable
@Composable
private fun chooseSchemeType(
  regular: RegularColorSchemeType,
  dark: DarkColorSchemeType,
) = when (regular) {
  RegularColorSchemeType.Light -> ColorSchemeType.Light
  RegularColorSchemeType.Dark -> dark.toColorSchemeType()
  RegularColorSchemeType.System -> if (isSystemInDarkTheme()) dark.toColorSchemeType() else ColorSchemeType.Light
}

@Stable
private fun DarkColorSchemeType.toColorSchemeType(): ColorSchemeType = when (this) {
  DarkColorSchemeType.Dark -> ColorSchemeType.Dark
  DarkColorSchemeType.Midnight -> ColorSchemeType.Midnight
}

@Composable
private fun Content(
  isServerUrlSet: Boolean,
  loginToken: LoginToken?,
  bottomBarState: BottomBarState,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier,
    verticalArrangement = Arrangement.Bottom,
  ) {
    ActualNavHost(
      modifier = Modifier.weight(1f),
      isServerUrlSet = isServerUrlSet,
      loginToken = loginToken,
    )

    if (bottomBarState is BottomBarState.Visible) {
      BottomStatusBar(
        modifier = Modifier.wrapContentHeight(),
        state = bottomBarState,
      )
    }

    // space to block out the bottom navigation bar, so we don't need to adjust layouts to account for it
    val navigationBarHeight = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    VerticalSpacer(navigationBarHeight)
  }
}
