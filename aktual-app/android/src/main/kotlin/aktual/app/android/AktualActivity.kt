package aktual.app.android

import aktual.app.nav.AktualAppContent
import aktual.app.nav.rememberBackStack
import aktual.di.AppScope
import android.app.Activity
import android.os.Bundle
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
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory
import dev.zacsweers.metrox.viewmodel.MetroViewModelFactory
import kotlinx.coroutines.flow.map

@ActivityKey
@ContributesIntoMap(AppScope::class, binding<Activity>())
class AktualActivity(override val defaultViewModelProviderFactory: MetroViewModelFactory) :
  ComponentActivity() {
  private val viewModel by viewModels<AktualActivityViewModel> { defaultViewModelProviderFactory }

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

    setContent { Content(viewModel, defaultViewModelProviderFactory) }
  }
}

@Composable
@Suppress("ViewModelForwarding")
private fun Content(viewModel: AktualActivityViewModel, viewModelFactory: MetroViewModelFactory) {
  val factoryFlow = remember {
    viewModel.runLevels.viewModelGraph().map { it.metroViewModelFactory }
  }
  val factory by factoryFlow.collectAsState(initial = viewModelFactory)

  CompositionLocalProvider(LocalMetroViewModelFactory provides factory) {
    val backStack = rememberBackStack(viewModel) ?: return@CompositionLocalProvider
    AktualAppContent(viewModel, backStack)
  }
}
