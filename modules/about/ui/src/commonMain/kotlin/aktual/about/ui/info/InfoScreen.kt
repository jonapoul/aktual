/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.about.ui.info

import aktual.about.vm.AboutViewModel
import aktual.about.vm.CheckUpdatesState
import aktual.core.ui.LocalTheme
import aktual.core.ui.metroViewModel
import alakazam.kotlin.core.noOp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun InfoScreen(
  nav: InfoNavigator,
  viewModel: AboutViewModel = metroViewModel(),
) {
  val theme = LocalTheme.current
  val buildState by viewModel.buildState.collectAsStateWithLifecycle()

  val onCancel = {
    viewModel.cancelUpdateCheck()
  }

  val checkUpdatesState by viewModel.checkUpdatesState.collectAsStateWithLifecycle()
  when (val state = checkUpdatesState) {
    CheckUpdatesState.Inactive -> noOp()
    CheckUpdatesState.Checking -> CheckUpdatesLoadingDialog(onCancel, theme = theme)
    CheckUpdatesState.NoUpdateFound -> NoUpdateFoundDialog(onCancel, theme = theme)
    is CheckUpdatesState.Failed -> UpdateCheckFailedDialog(state.cause, onCancel, theme = theme)

    is CheckUpdatesState.UpdateFound -> UpdateFoundDialog(
      currentVersion = buildState.versions.app,
      latestVersion = state.version,
      latestUrl = state.url,
      onDismiss = onCancel,
      onOpenUrl = { url -> viewModel.openUrl(url) },
      theme = theme,
    )
  }

  InfoScaffold(
    buildState = buildState,
    onAction = { action ->
      when (action) {
        InfoAction.OpenSourceCode -> viewModel.openRepo()
        InfoAction.ReportIssue -> viewModel.reportIssues()
        InfoAction.CheckUpdates -> viewModel.fetchLatestRelease()
        InfoAction.NavBack -> nav.back()
        InfoAction.ViewLicenses -> nav.toLicenses()
      }
    },
  )
}
