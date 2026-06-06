package aktual.about.ui.info

import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.Warning
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter

@Composable
internal fun UpdateCheckFailedDialog(
  cause: String,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  AktualAlertDialog(
    modifier = modifier,
    onDismissRequest = onDismiss,
    content = {
      UpdateCheckFailedDialogContent(cause = cause, onDismiss = onDismiss)
    },
  )
}

@Composable
internal fun UpdateCheckFailedDialogContent(
  cause: String,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  AktualAlertDialogContent(
    modifier = modifier,
    title = Strings.infoCheckFailedTitle,
    icon = MaterialIcons.Warning,
    titleColor = colors.errorText,
    content = { Text(cause) },
    buttons = {
      TextButton(onClick = onDismiss) {
        Text(text = Strings.infoCheckFailedOk, color = colors.errorText)
      }
    },
  )
}

@Preview
@Composable
private fun PreviewCheckFailedContent(@PreviewParameter(ColoredParameters::class) colors: Colors) =
  PreviewWithColors(colors) {
    UpdateCheckFailedDialogContent(
      cause =
        "Something broke lol. And here's some other rubbish to show how the text looks when wrapping lines",
      onDismiss = {},
    )
  }
