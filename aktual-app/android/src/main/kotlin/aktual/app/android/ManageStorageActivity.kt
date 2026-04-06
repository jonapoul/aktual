package aktual.app.android

import aktual.about.ui.storage.ManageStorageScreen
import aktual.core.ui.AktualTheme
import aktual.core.ui.DialogBlurOverlay
import aktual.core.ui.DialogBlurState
import aktual.core.ui.WithCompositionLocals
import aktual.nav.core.BackHandler
import aktual.nav.core.rememberAppCloser
import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory
import dev.zacsweers.metrox.viewmodel.MetroViewModelFactory

@ActivityKey
@ContributesIntoMap(AppScope::class, binding<Activity>())
class ManageStorageActivity(override val defaultViewModelProviderFactory: MetroViewModelFactory) :
  ComponentActivity() {
  private val viewModel by
    viewModels<ManageStorageActivityViewModel> { defaultViewModelProviderFactory }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    installSplashScreen()

    val transparent = Color.Transparent.value.toInt()
    enableEdgeToEdge(
      statusBarStyle = SystemBarStyle.auto(transparent, transparent),
      navigationBarStyle = SystemBarStyle.auto(transparent, transparent),
    )

    setContent {
      val closeApp = rememberAppCloser()
      BackHandler { closeApp() }

      val hazeState = rememberHazeState()
      val isSystemDark = isSystemInDarkTheme()
      LaunchedEffect(isSystemDark) { viewModel.updateSystemDarkTheme(isSystemDark) }
      val theme by viewModel.theme.collectAsStateWithLifecycle()

      WithCompositionLocals(
        hazeState = hazeState,
        dialogBlurState = remember { DialogBlurState() },
      ) {
        AktualTheme(theme) {
          Box(contentAlignment = Alignment.BottomCenter) {
            CompositionLocalProvider(
              LocalMetroViewModelFactory provides defaultViewModelProviderFactory
            ) {
              ManageStorageScreen(
                modifier =
                  Modifier.fillMaxSize()
                    .consumeWindowInsets(WindowInsets.navigationBars)
                    .hazeSource(hazeState),
                navBack = { closeApp() },
                onStorageNavEvent = {}, // no stack to modify
              )
            }

            DialogBlurOverlay()
          }
        }
      }
    }
  }
}
