package aktual.about.ui.info

import androidx.compose.runtime.Immutable

@Immutable
internal sealed interface InfoAction {
  data object NavBack : InfoAction
  data object ViewLicenses : InfoAction
  data object CheckUpdates : InfoAction
  data object ReportIssue : InfoAction
  data object OpenSourceCode : InfoAction
}
