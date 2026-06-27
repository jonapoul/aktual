package aktual.app.android

import aktual.app.nav.AktualAppContent
import aktual.app.nav.rememberBackStack
import aktual.core.ui.LoadingScreenIfNotNull
import aktual.di.AppScope
import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory
import dev.zacsweers.metrox.viewmodel.MetroViewModelFactory
import kotlinx.coroutines.launch

@ActivityKey
@ContributesIntoMap(AppScope::class, binding<Activity>())
class AktualActivity(override val defaultViewModelProviderFactory: MetroViewModelFactory) :
  ComponentActivity() {
  private val viewModel by viewModels<AktualActivityViewModel>()

  override fun onDestroy() {
    super.onDestroy()
    viewModel.onDestroy()
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    installSplashScreen()

    val transparent = Color.Transparent.value.toInt()
    enableEdgeToEdge(
      statusBarStyle = SystemBarStyle.auto(transparent, transparent),
      navigationBarStyle = SystemBarStyle.auto(transparent, transparent),
    )

    // toggle FLAG_SECURE so the app's contents are hidden in the recents switcher (and screenshots
    // are blocked) according to the user's preference. The flow seeds with the default (true), so
    // we're secure-by-default until DataStore reports otherwise
    lifecycleScope.launch {
      repeatOnLifecycle(Lifecycle.State.STARTED) {
        viewModel.hidePreviewInAppSwitcher.collect { hide ->
          if (hide) {
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
          } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
          }
        }
      }
    }

    setContent { Content(viewModel) }
  }
}

@Composable
@Suppress("ViewModelForwarding")
@SuppressLint("ComposeViewModelForwarding")
private fun Content(viewModel: AktualActivityViewModel) {
  val viewModelFactory by remember {
    viewModel.runLevels.viewModelFactory()
  }
    .collectAsState(initial = null)

  LoadingScreenIfNotNull(viewModelFactory) { vmf ->
    CompositionLocalProvider(LocalMetroViewModelFactory provides vmf) {
      val backStack = rememberBackStack(viewModel) ?: return@CompositionLocalProvider
      AktualAppContent(viewModel, backStack)
    }
  }
}
