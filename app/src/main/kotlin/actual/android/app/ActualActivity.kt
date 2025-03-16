package actual.android.app

import actual.core.ui.ActualTheme
import actual.core.ui.LocalColorSchemeType
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
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
      CompositionLocalProvider(LocalColorSchemeType provides scheme) {
        ActualTheme(scheme) {
          ActualNavHost(
            isServerUrlSet = viewModel.isServerUrlSet,
            loginToken = viewModel.loginToken,
          )
        }
      }
    }
  }
}
