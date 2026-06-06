package aktual.about.ui.info

import aktual.core.l10n.Strings
import aktual.core.theme.Colors
import aktual.core.ui.AktualAlertDialog
import aktual.core.ui.AktualAlertDialogContent
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.ColoredParameters
import aktual.core.ui.PreviewWithColors
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter

@Composable
internal fun UpdateFoundDialog(
  currentVersion: String,
  latestVersion: String,
  latestUrl: String,
  onDismiss: () -> Unit,
  onOpenUrl: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  AktualAlertDialog(
    modifier = modifier.testTag(Tags.UpdateFoundDialog),
    onDismissRequest = onDismiss,
    content = {
      UpdateFoundDialogContent(
        currentVersion = currentVersion,
        latestVersion = latestVersion,
        latestUrl = latestUrl,
        onDismiss = onDismiss,
        onOpenUrl = onOpenUrl,
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
) {
  AktualAlertDialogContent(
    modifier = modifier,
    title = Strings.infoUpdateFoundTitle,
    titleColor = colors.noticeText,
    content = {
      TextContent(
        Strings.infoUpdateFoundInstalled,
        currentVersion,
        Tags.UpdateAvailableCurrentVersion,
      )
      TextContent(Strings.infoUpdateFoundLatest, latestVersion, Tags.UpdateAvailableNewVersion)
    },
    buttons = {
      TextButton(onClick = onDismiss) {
        Text(text = Strings.infoUpdateFoundDismiss, color = colors.noticeText)
      }
      TextButton(
        modifier = Modifier.testTag(Tags.UpdateAvailableDownloadButton),
        onClick = {
          onOpenUrl(latestUrl)
          onDismiss()
        },
      ) {
        Text(text = Strings.infoUpdateFoundView, color = colors.noticeText)
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
  Row(modifier = modifier.fillMaxWidth()) {
    Text(
      modifier = Modifier.weight(1f),
      text = text,
      textAlign = TextAlign.Start,
      fontWeight = FontWeight.Bold,
    )
    Text(
      modifier = Modifier.weight(1f).testTag(valueNodeTag),
      text = value,
      textAlign = TextAlign.End,
    )
  }
}

@Preview
@Composable
private fun PreviewUpdateFoundContent(@PreviewParameter(ColoredParameters::class) colors: Colors) =
  PreviewWithColors(colors) {
    UpdateFoundDialogContent(
      currentVersion = "v1.2.3",
      latestVersion = "v2.3.4",
      latestUrl = "https://github.com/jonapoul/aktual/releases/v2.3.4",
      onOpenUrl = {},
      onDismiss = {},
    )
  }
