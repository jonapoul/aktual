package aktual.about.ui.info

import aktual.core.model.ColorSchemeType
import aktual.core.ui.ColorSchemeParameters
import aktual.core.ui.NormalTextButton
import aktual.core.ui.PreviewWithColorScheme
import aktual.l10n.Strings
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp

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

@Preview
@Composable
private fun PreviewInfoButtons(
  @PreviewParameter(ColorSchemeParameters::class) type: ColorSchemeType,
) = PreviewWithColorScheme(type) {
  InfoButtons(
    modifier = Modifier.width(300.dp),
    onAction = {},
  )
}
