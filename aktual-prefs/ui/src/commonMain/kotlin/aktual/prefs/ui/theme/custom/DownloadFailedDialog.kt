package aktual.prefs.ui.theme.custom

import aktual.core.icons.AktualIcons
import aktual.core.icons.CloudWarning
import aktual.core.l10n.Strings
import aktual.core.theme.Colors
import aktual.core.ui.AktualAlertDialog
import aktual.core.ui.AktualAlertDialogContent
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.ColoredParameters
import aktual.core.ui.PreviewWithColors
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter

@Composable
internal fun DownloadFailedDialog(
  failure: DownloadFailure,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  AktualAlertDialog(
    modifier = modifier,
    onDismissRequest = onDismiss,
    content = { DownloadFailedDialogContent(failure = failure, onDismiss = onDismiss) },
  )
}

@Immutable internal data class DownloadFailure(val name: String, val reason: String)

@Composable
private fun DownloadFailedDialogContent(
  failure: DownloadFailure,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  AktualAlertDialogContent(
    modifier = modifier,
    title = Strings.settingsThemeDownloadFailedTitle(failure.name),
    icon = AktualIcons.CloudWarning,
    highlight = colors.errorText,
    content = { Text(failure.reason) },
    buttons = {
      TextButton(onClick = onDismiss) {
        Text(text = Strings.settingsThemeDownloadFailedOk, color = colors.errorText)
      }
    },
  )
}

@Preview
@Composable
private fun PreviewDownloadFailedContent(
  @PreviewParameter(ColoredParameters::class) colors: Colors
) =
  PreviewWithColors(colors) {
    DownloadFailedDialogContent(
      failure =
        DownloadFailure(
          name = "My theme",
          reason = "Something broke. And here's some more text to show how it looks when wrapping",
        ),
      onDismiss = {},
    )
  }
