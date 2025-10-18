/**
 * Copyright 2025 Jon Poulton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package actual.about.ui.info

import actual.about.vm.AboutViewModel
import actual.about.vm.CheckUpdatesState
import actual.core.ui.LocalTheme
import actual.core.ui.metroViewModel
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
