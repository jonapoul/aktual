/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.app.desktop

import aktual.app.di.ViewModelFactory
import aktual.app.nav.AktualAppContent
import aktual.core.ui.AktualTheme
import aktual.core.ui.LocalViewModelGraphProvider
import aktual.core.ui.WithCompositionLocals
import aktual.core.ui.chooseSchemeType
import aktual.core.ui.metroViewModel
import aktual.l10n.Drawables
import aktual.l10n.Strings
import aktual.logging.JvmLogStorage
import aktual.logging.LogbackLogger
import aktual.logging.TimestampedPrintStreamLogger
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import logcat.LogPriority
import logcat.LogcatLogger
import logcat.logcat

fun main() = application(exitProcessOnExit = true) {
  val viewModelStoreOwner = remember { JvmViewModelStoreOwner() }
  val graphHolder = remember { JvmAppGraphHolder() }
  val graph = remember(graphHolder) { graphHolder.get() }
  val factory = remember(graph) { ViewModelFactory(graph) }
  val windowPrefs = remember(graph) { graph.windowPreferences }

  LaunchedEffect(Unit) {
    initLogging(graph)
  }

  val state = rememberWindowState(
    placement = windowPrefs.placement.get(),
    isMinimized = windowPrefs.isMinimized.get(),
    position = windowPrefs.position.get(),
    size = windowPrefs.size.get(),
  )

  val viewModel = metroViewModel<AktualDesktopViewModel>(
    owner = viewModelStoreOwner,
    graphProvider = factory,
  )

  LaunchedEffect(Unit) {
    viewModel.start()
  }

  Window(
    title = Strings.appName,
    icon = Drawables.appIcon48,
    resizable = true,
    enabled = true,
    focusable = true,
    state = state,
    onCloseRequest = {
      logcat.i { "onCloseRequest" }
      windowPrefs.save(state)
      viewModel.onDestroy()
      exitApplication()
    },
  ) {
    WindowContents(
      viewModel = viewModel,
      viewModelStoreOwner = viewModelStoreOwner,
      factory = factory,
    )
  }
}

@Composable
private fun WindowContents(
  viewModel: AktualDesktopViewModel,
  viewModelStoreOwner: ViewModelStoreOwner,
  factory: ViewModelFactory,
) = CompositionLocalProvider(
  LocalViewModelStoreOwner provides viewModelStoreOwner,
  LocalViewModelGraphProvider provides factory,
) {
  val regular by viewModel.regularSchemeType.collectAsState()
  val darkScheme by viewModel.darkSchemeType.collectAsState()
  val bottomBarState by viewModel.bottomBarState.collectAsState()
  val numberFormat by viewModel.numberFormat.collectAsState()
  val hideFraction by viewModel.hideFraction.collectAsState()
  val isPrivacyEnabled by viewModel.isPrivacyEnabled.collectAsState()
  val colorSchemeType = chooseSchemeType(regular, darkScheme)

  WithCompositionLocals(
    isPrivacyEnabled = isPrivacyEnabled,
    format = numberFormat,
    hideFractions = hideFraction,
  ) {
    AktualTheme(colorSchemeType) {
      AktualAppContent(
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

private fun initLogging(graph: JvmAppGraph) {
  val logStorage = JvmLogStorage()
  val minPriority = LogPriority.VERBOSE
  with(LogcatLogger) {
    install()
    loggers += TimestampedPrintStreamLogger(System.out, graph.clock, minPriority)
    loggers += LogbackLogger(logStorage, minPriority)
  }

  logcat.i { "onCreate" }
  logcat.d { "buildConfig = ${graph.buildConfig}" }
}
