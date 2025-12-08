package aktual.app.android

import aktual.app.nav.AktualAppContent
import aktual.core.di.ActivityKey
import aktual.core.ui.AktualTheme
import aktual.core.ui.WithCompositionLocals
import aktual.core.ui.chooseSchemeType
import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
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

    setContent {
      Content(
        viewModel = viewModel,
        viewModelFactory = viewModelFactory,
      )
    }
  }
}

@Composable
private fun Content(
  viewModel: AktualActivityViewModel,
  viewModelFactory: MetroViewModelFactory,
) {
  val regular by viewModel.regularSchemeType.collectAsStateWithLifecycle()
  val darkScheme by viewModel.darkSchemeType.collectAsStateWithLifecycle()
  val bottomBarState by viewModel.bottomBarState.collectAsStateWithLifecycle()
  val numberFormat by viewModel.numberFormat.collectAsStateWithLifecycle()
  val hideFraction by viewModel.hideFraction.collectAsStateWithLifecycle()
  val isPrivacyEnabled by viewModel.isPrivacyEnabled.collectAsStateWithLifecycle()
  val colorSchemeType = chooseSchemeType(regular, darkScheme)

  CompositionLocalProvider(
    LocalMetroViewModelFactory provides viewModelFactory,
  ) {
    WithCompositionLocals(
      isPrivacyEnabled = isPrivacyEnabled,
      format = numberFormat,
      hideFractions = hideFraction,
    ) {
      AktualTheme(colorSchemeType) {
        AktualAppContent(
          navController = rememberNavController(),
          isPrivacyEnabled = isPrivacyEnabled,
          numberFormat = numberFormat,
          hideFraction = hideFraction,
          colorSchemeType = colorSchemeType,
          isServerUrlSet = viewModel.isServerUrlSet,
          loginToken = viewModel.loginToken,
          bottomBarState = bottomBarState,
        )
      }
    }
  }
}
