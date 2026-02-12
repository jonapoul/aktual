package aktual.about.ui.info

import aktual.core.icons.MaterialIcons
import aktual.core.icons.Warning
import aktual.core.l10n.Strings
import aktual.core.model.ColorSchemeType
import aktual.core.ui.ColorSchemeParameters
import aktual.core.ui.DialogContent
import aktual.core.ui.LocalTheme
import aktual.core.ui.PreviewWithColorScheme
import aktual.core.ui.Theme
import androidx.compose.material3.BasicAlertDialog
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
  theme: Theme = LocalTheme.current,
) {
  BasicAlertDialog(
    modifier = modifier,
    onDismissRequest = onDismiss,
    content = {
      UpdateCheckFailedDialogContent(cause = cause, onDismiss = onDismiss, theme = theme)
    },
  )
}

@Composable
internal fun UpdateCheckFailedDialogContent(
  cause: String,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  DialogContent(
    modifier = modifier,
    theme = theme,
    title = Strings.infoCheckFailedTitle,
    icon = MaterialIcons.Warning,
    titleColor = theme.errorText,
    content = { Text(cause) },
    buttons = {
      TextButton(onClick = onDismiss) {
        Text(text = Strings.infoCheckFailedOk, color = theme.errorText)
      }
    },
  )
}

@Preview
@Composable
private fun PreviewCheckFailedContent(
  @PreviewParameter(ColorSchemeParameters::class) type: ColorSchemeType
) =
  PreviewWithColorScheme(type) {
    UpdateCheckFailedDialogContent(
      cause =
        "Something broke lol. And here's some other rubbish to show how the text looks when wrapping lines",
      onDismiss = {},
    )
  }
