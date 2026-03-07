package aktual.app.android

import aktual.app.nav.AktualAppContent
import aktual.core.di.ActivityKey
import aktual.core.ui.AktualTheme
import aktual.core.ui.WithCompositionLocals
import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
private fun Content(viewModel: AktualActivityViewModel, viewModelFactory: MetroViewModelFactory) {
  val theme by viewModel.theme(isSystemInDarkTheme()).collectAsStateWithLifecycle(null)
  val bottomBarState by viewModel.bottomBarState.collectAsStateWithLifecycle()
  val numberFormat by viewModel.numberFormat.collectAsStateWithLifecycle()
  val hideFraction by viewModel.hideFraction.collectAsStateWithLifecycle()
  val isPrivacyEnabled by viewModel.isPrivacyEnabled.collectAsStateWithLifecycle()

  theme?.let { t ->
    CompositionLocalProvider(LocalMetroViewModelFactory provides viewModelFactory) {
      WithCompositionLocals(
        isPrivacyEnabled = isPrivacyEnabled,
        format = numberFormat,
        hideFraction = hideFraction,
      ) {
        AktualTheme(t) {
          AktualAppContent(
            navController = rememberNavController(),
            isPrivacyEnabled = isPrivacyEnabled,
            numberFormat = numberFormat,
            hideFraction = hideFraction,
            isServerUrlSet = viewModel.isServerUrlSet,
            token = viewModel.token,
            bottomBarState = bottomBarState,
          )
        }
      }
    }
  }
}
