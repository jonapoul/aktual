package actual.about.ui

import actual.about.res.AboutStrings
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
internal fun AboutButtons(
  onAction: (AboutAction) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier.fillMaxWidth(),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    val buttonModifier = Modifier.fillMaxWidth()

    NormalTextButton(
      modifier = buttonModifier.testTag(Tags.CheckUpdatesButton),
      text = AboutStrings.checkUpdates,
      onClick = { onAction(AboutAction.CheckUpdates) },
    )
    NormalTextButton(
      modifier = buttonModifier.testTag(Tags.ReportButton),
      text = AboutStrings.reportIssues,
      onClick = { onAction(AboutAction.ReportIssue) },
    )
    NormalTextButton(
      modifier = buttonModifier.testTag(Tags.LicensesButton),
      text = AboutStrings.licenses,
      onClick = { onAction(AboutAction.ViewLicenses) },
    )
  }
}

@Preview
@Composable
private fun PreviewAboutButtons() = PreviewColumn {
  AboutButtons(
    modifier = Modifier.width(300.dp),
    onAction = {},
  )
}
