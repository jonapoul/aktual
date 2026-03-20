package aktual.app.desktop

import aktual.app.nav.AktualAppContent
import aktual.app.nav.rememberBackStack
import aktual.core.l10n.Drawables
import aktual.core.l10n.Strings
import aktual.core.logging.JvmLogStorage
import aktual.core.logging.KermitFileLogger
import aktual.core.logging.TimestampedPrintStreamLogger
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.zacsweers.metro.createGraph
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory
import logcat.LogPriority
import logcat.LogcatLogger
import logcat.logcat

private const val TAG = "Main"

fun main() {
  val graph = createGraph<JvmAppGraph>()

  val logStorage = JvmLogStorage()
  val minPriority = LogPriority.VERBOSE
  with(LogcatLogger) {
    install()
    loggers += TimestampedPrintStreamLogger(System.out, graph.clock, minPriority)
    loggers += KermitFileLogger(logStorage, minPriority)
  }

  logcat.i(TAG) { "App started" }
  logcat.d(TAG) { "buildConfig = ${graph.buildConfig}" }

  val viewModelStoreOwner = JvmViewModelStoreOwner()
  composeApp(graph, viewModelStoreOwner)
}

private fun composeApp(graph: JvmAppGraph, viewModelStoreOwner: JvmViewModelStoreOwner) {
  application(exitProcessOnExit = true) {
    val windowPrefs = remember(graph) { graph.windowPreferences }
    val state =
      rememberWindowState(
        placement = windowPrefs.placement.get(),
        isMinimized = windowPrefs.isMinimized.get(),
        position = windowPrefs.position.get(),
        size = windowPrefs.size.get(),
      )

    val viewModel =
      viewModel<AktualDesktopViewModel>(
        viewModelStoreOwner = viewModelStoreOwner,
        factory = graph.metroViewModelFactory,
      )

    val backStack = rememberBackStack(viewModel)
    val keyHandler = remember { KeyboardEventHandler(backStack) }

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
      CompositionLocalProvider(
        LocalViewModelStoreOwner provides viewModelStoreOwner,
        LocalMetroViewModelFactory provides graph.metroViewModelFactory,
      ) {
        AktualAppContent(viewModel = viewModel, backStack = backStack)
      }
    }
  }
}
