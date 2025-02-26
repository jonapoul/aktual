package actual.about.info.ui

import actual.about.info.vm.CheckUpdatesState
import actual.about.info.vm.InfoViewModel
import actual.about.licenses.nav.LicensesNavRoute
import actual.core.ui.LocalTheme
import alakazam.kotlin.core.noOp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController

@Composable
fun InfoScreen(
  navController: NavController,
  viewModel: InfoViewModel = hiltViewModel(),
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
      currentVersion = buildState.buildVersion,
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
        InfoAction.NavBack -> navController.popBackStack()
        InfoAction.ViewLicenses -> navController.navigate(LicensesNavRoute)
      }
    },
  )
}
