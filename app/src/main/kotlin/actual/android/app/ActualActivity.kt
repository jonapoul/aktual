package actual.android.app

import actual.core.ui.ActualTheme
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
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
    enableEdgeToEdge()

    setContent {
      val scheme by viewModel.colorSchemeType.collectAsStateWithLifecycle()
      ActualTheme(scheme) {
        ActualNavHost(
          isServerUrlSet = viewModel.isServerUrlSet,
          loginToken = viewModel.loginToken,
        )
      }
    }
  }
}
