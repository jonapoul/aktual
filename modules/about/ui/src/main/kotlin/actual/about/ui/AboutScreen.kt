package actual.about.ui

import actual.about.vm.AboutViewModel
import actual.about.vm.CheckUpdatesState
import actual.core.ui.LocalTheme
import actual.core.ui.set
import actual.licenses.nav.LicensesNavRoute
import alakazam.kotlin.core.noOp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController

@Composable
fun AboutScreen(
  navController: NavController,
  viewModel: AboutViewModel = hiltViewModel(),
) {
  val theme = LocalTheme.current
  val buildState by viewModel.buildState.collectAsStateWithLifecycle()

  val checkUpdates = remember { mutableStateOf(false) }
  val onCancel = {
    checkUpdates.set(false)
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

  if (checkUpdates.value) {
    LaunchedEffect(Unit) { viewModel.fetchLatestRelease() }
  }

  AboutScaffold(
    buildState = buildState,
    onAction = { action ->
      when (action) {
        AboutAction.OpenSourceCode -> viewModel.openRepo()
        AboutAction.ReportIssue -> viewModel.reportIssues()
        AboutAction.CheckUpdates -> checkUpdates.set(true)
        AboutAction.NavBack -> navController.popBackStack()
        AboutAction.ViewLicenses -> navController.navigate(LicensesNavRoute)
      }
    },
  )
}
