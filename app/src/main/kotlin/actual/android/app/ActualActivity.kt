package actual.android.app

import actual.account.model.LoginToken
import actual.android.app.nav.ActualNavHost
import actual.core.ui.ActualTheme
import actual.core.ui.LocalColorSchemeType
import alakazam.android.ui.compose.VerticalSpacer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ActualActivity : ComponentActivity() {
  private val viewModel by viewModels<ActualActivityViewModel>()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    viewModel.start()

    installSplashScreen()

    val transparent = Transparent.value.toInt()
    enableEdgeToEdge(
      statusBarStyle = SystemBarStyle.auto(transparent, transparent),
      navigationBarStyle = SystemBarStyle.auto(transparent, transparent),
    )

    setContent {
      val scheme by viewModel.colorSchemeType.collectAsStateWithLifecycle()
      val bottomBarState by viewModel.bottomBarState.collectAsStateWithLifecycle()

      CompositionLocalProvider(LocalColorSchemeType provides scheme) {
        ActualTheme(scheme) {
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
