package aktual.about.ui.info

import aktual.core.l10n.Strings
import aktual.core.theme.LocalTheme
import aktual.core.theme.Theme
import aktual.core.ui.AlertDialog
import aktual.core.ui.DialogContent
import aktual.core.ui.PreviewWithTheme
import aktual.core.ui.ThemeParameters
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter

@Composable
internal fun NoUpdateFoundDialog(
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  AlertDialog(
    modifier = modifier,
    onDismissRequest = onDismiss,
    content = { NoUpdateFoundDialogContent(onDismiss = onDismiss, theme = theme) },
  )
}

@Composable
internal fun NoUpdateFoundDialogContent(
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  DialogContent(
    modifier = modifier,
    theme = theme,
    title = Strings.infoNoUpdateTitle,
    content = { Text(text = Strings.infoNoUpdateMessage) },
    buttons = {
      TextButton(onClick = onDismiss) {
        Text(text = Strings.infoNoUpdateOk, color = theme.pageTextPositive)
      }
    },
  )
}

@Preview
@Composable
private fun PreviewNoUpdatesContent(@PreviewParameter(ThemeParameters::class) theme: Theme) =
  PreviewWithTheme(theme) { NoUpdateFoundDialogContent(onDismiss = {}) }
