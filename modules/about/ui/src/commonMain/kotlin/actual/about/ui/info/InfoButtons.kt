package actual.about.ui.info

import actual.core.ui.NormalTextButton
import actual.l10n.Strings
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

@Composable
internal fun InfoButtons(
  onAction: (InfoAction) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier.fillMaxWidth(),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    val buttonModifier = Modifier.fillMaxWidth()

    NormalTextButton(
      modifier = buttonModifier.testTag(Tags.SourceCodeButton),
      text = Strings.infoRepo,
      onClick = { onAction(InfoAction.OpenSourceCode) },
    )
    NormalTextButton(
      modifier = buttonModifier.testTag(Tags.CheckUpdatesButton),
      text = Strings.infoCheckUpdates,
      onClick = { onAction(InfoAction.CheckUpdates) },
    )
    NormalTextButton(
      modifier = buttonModifier.testTag(Tags.ReportButton),
      text = Strings.infoReportIssues,
      onClick = { onAction(InfoAction.ReportIssue) },
    )
    NormalTextButton(
      modifier = buttonModifier.testTag(Tags.LicensesButton),
      text = Strings.infoLicenses,
      onClick = { onAction(InfoAction.ViewLicenses) },
    )
  }
}
