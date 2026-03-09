package aktual.app.android

import aktual.app.nav.AktualAppContent
import aktual.core.di.ActivityKey
import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory
import dev.zacsweers.metrox.viewmodel.MetroViewModelFactory

@Inject
@ActivityKey(AktualActivity::class)
@ContributesIntoMap(AppScope::class, binding<Activity>())
class AktualActivity(private val viewModelFactory: MetroViewModelFactory) : ComponentActivity() {
  override val defaultViewModelProviderFactory = viewModelFactory

  private val viewModel by viewModels<AktualActivityViewModel> { viewModelFactory }

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

    setContent { Content(viewModel = viewModel, viewModelFactory = viewModelFactory) }
  }
}

@Composable
@Suppress("ViewModelForwarding")
private fun Content(viewModel: AktualActivityViewModel, viewModelFactory: MetroViewModelFactory) {
  CompositionLocalProvider(LocalMetroViewModelFactory provides viewModelFactory) {
    AktualAppContent(viewModel = viewModel, navController = rememberNavController())
  }
}
