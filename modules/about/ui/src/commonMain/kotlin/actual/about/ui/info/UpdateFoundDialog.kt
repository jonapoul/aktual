@file:OptIn(ExperimentalMaterial3Api::class)

package actual.about.ui.info

import actual.core.ui.DialogContent
import actual.core.ui.LocalTheme
import actual.core.ui.Theme
import actual.l10n.Strings
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign

@Composable
internal fun UpdateFoundDialog(
  currentVersion: String,
  latestVersion: String,
  latestUrl: String,
  onDismiss: () -> Unit,
  onOpenUrl: (String) -> Unit,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  BasicAlertDialog(
    modifier = modifier.testTag(Tags.UpdateFoundDialog),
    onDismissRequest = onDismiss,
    content = {
      UpdateFoundDialogContent(
        currentVersion = currentVersion,
        latestVersion = latestVersion,
        latestUrl = latestUrl,
        onDismiss = onDismiss,
        onOpenUrl = onOpenUrl,
        theme = theme,
      )
    },
  )
}

@Composable
internal fun UpdateFoundDialogContent(
  currentVersion: String,
  latestVersion: String,
  latestUrl: String,
  onOpenUrl: (String) -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  DialogContent(
    modifier = modifier,
    theme = theme,
    title = Strings.infoUpdateFoundTitle,
    titleColor = theme.successText,
    content = {
      TextContent(Strings.infoUpdateFoundInstalled, currentVersion, Tags.UpdateAvailableCurrentVersion)
      TextContent(Strings.infoUpdateFoundLatest, latestVersion, Tags.UpdateAvailableNewVersion)
    },
    buttons = {
      TextButton(onClick = onDismiss) {
        Text(
          text = Strings.infoUpdateFoundDismiss,
          color = theme.successText,
        )
      }
      TextButton(
        modifier = Modifier.testTag(Tags.UpdateAvailableDownloadButton),
        onClick = {
          onOpenUrl(latestUrl)
          onDismiss()
        },
      ) {
        Text(
          text = Strings.infoUpdateFoundView,
          color = theme.successText,
        )
      }
    },
  )
}

@Composable
private fun TextContent(
  text: String,
  value: String,
  valueNodeTag: String,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier.fillMaxWidth(),
  ) {
    Text(
      modifier = Modifier.weight(1f),
      text = text,
      textAlign = TextAlign.Start,
      fontWeight = FontWeight.Bold,
    )
    Text(
      modifier = Modifier
        .weight(1f)
        .testTag(valueNodeTag),
      text = value,
      textAlign = TextAlign.End,
    )
  }
}
