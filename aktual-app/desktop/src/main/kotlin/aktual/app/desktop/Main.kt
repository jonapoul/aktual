package aktual.app.desktop

import aktual.app.nav.AktualAppContent
import aktual.core.l10n.Drawables
import aktual.core.l10n.Strings
import aktual.core.logging.JvmLogStorage
import aktual.core.logging.KermitFileLogger
import aktual.core.logging.TimestampedPrintStreamLogger
import aktual.core.ui.AktualTheme
import aktual.core.ui.WithCompositionLocals
import aktual.core.ui.chooseSchemeType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import dev.zacsweers.metro.createGraph
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory
import dev.zacsweers.metrox.viewmodel.MetroViewModelFactory
import logcat.LogPriority
import logcat.LogcatLogger
import logcat.logcat

fun main() {
  val graph = createGraph<JvmAppGraph>()

  val logStorage = JvmLogStorage()
  val minPriority = LogPriority.VERBOSE
  with(LogcatLogger) {
    install()
    loggers += TimestampedPrintStreamLogger(System.out, graph.clock, minPriority)
    loggers += KermitFileLogger(logStorage, minPriority)
  }

  logcat.i { "App started" }
  logcat.d { "buildConfig = ${graph.buildConfig}" }

  val viewModelStoreOwner = JvmViewModelStoreOwner()
  composeApp(graph, viewModelStoreOwner)
}

private fun composeApp(
  graph: JvmAppGraph,
  viewModelStoreOwner: JvmViewModelStoreOwner,
) = application(exitProcessOnExit = true) {
  val windowPrefs = remember(graph) { graph.windowPreferences }
  val state = rememberWindowState(
    placement = windowPrefs.placement.get(),
    isMinimized = windowPrefs.isMinimized.get(),
    position = windowPrefs.position.get(),
    size = windowPrefs.size.get(),
  )

  val viewModel = viewModel<AktualDesktopViewModel>(
    viewModelStoreOwner = viewModelStoreOwner,
    factory = graph.metroViewModelFactory,
  )

  val navController = rememberNavController()
  val keyHandler = remember { KeyboardEventHandler(navController) }

  Window(
    title = Strings.appName,
    icon = Drawables.appIcon192,
    resizable = true,
    enabled = true,
    focusable = true,
    state = state,
    onKeyEvent = keyHandler::onKeyEvent,
    onPreviewKeyEvent = keyHandler::onPreviewKeyEvent,
    onCloseRequest = {
      logcat.i { "onCloseRequest" }
      windowPrefs.save(state)
      viewModel.onDestroy()
      exitApplication()
    },
  ) {
    WindowContents(
      navController = navController,
      viewModel = viewModel,
      viewModelStoreOwner = viewModelStoreOwner,
      factory = graph.metroViewModelFactory,
    )
  }
}

@Composable
private fun WindowContents(
  navController: NavHostController,
  viewModel: AktualDesktopViewModel,
  viewModelStoreOwner: ViewModelStoreOwner,
  factory: MetroViewModelFactory,
) = CompositionLocalProvider(
  LocalViewModelStoreOwner provides viewModelStoreOwner,
  LocalMetroViewModelFactory provides factory,
) {
  val regular by viewModel.regularSchemeType.collectAsStateWithLifecycle()
  val darkScheme by viewModel.darkSchemeType.collectAsStateWithLifecycle()
  val bottomBarState by viewModel.bottomBarState.collectAsStateWithLifecycle()
  val numberFormat by viewModel.numberFormat.collectAsStateWithLifecycle()
  val hideFraction by viewModel.hideFraction.collectAsStateWithLifecycle()
  val isPrivacyEnabled by viewModel.isPrivacyEnabled.collectAsStateWithLifecycle()
  val colorSchemeType = chooseSchemeType(regular, darkScheme)

  WithCompositionLocals(
    isPrivacyEnabled = isPrivacyEnabled,
    format = numberFormat,
    hideFraction = hideFraction,
  ) {
    AktualTheme(colorSchemeType) {
      AktualAppContent(
        navController = navController,
        isPrivacyEnabled = isPrivacyEnabled,
        numberFormat = numberFormat,
        hideFraction = hideFraction,
        colorSchemeType = colorSchemeType,
        isServerUrlSet = viewModel.isServerUrlSet,
        token = viewModel.token,
        bottomBarState = bottomBarState,
      )
    }
  }
}
