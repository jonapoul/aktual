package actual.about.info.ui

import actual.about.info.res.InfoStrings
import actual.core.ui.NormalTextButton
import actual.core.ui.PreviewColumn
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
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
      modifier = buttonModifier.testTag(Tags.CheckUpdatesButton),
      text = InfoStrings.checkUpdates,
      onClick = { onAction(InfoAction.CheckUpdates) },
    )
    NormalTextButton(
      modifier = buttonModifier.testTag(Tags.ReportButton),
      text = InfoStrings.reportIssues,
      onClick = { onAction(InfoAction.ReportIssue) },
    )
    NormalTextButton(
      modifier = buttonModifier.testTag(Tags.LicensesButton),
      text = InfoStrings.licenses,
      onClick = { onAction(InfoAction.ViewLicenses) },
    )
  }
}

@Preview
@Composable
private fun PreviewInfoButtons() = PreviewColumn {
  InfoButtons(
    modifier = Modifier.width(300.dp),
    onAction = {},
  )
}
