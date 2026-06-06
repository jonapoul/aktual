package aktual.about.ui.info

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
internal fun NoUpdateFoundDialog(
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  AktualAlertDialog(
    modifier = modifier,
    onDismissRequest = onDismiss,
    content = { NoUpdateFoundDialogContent(onDismiss = onDismiss) },
  )
}

@Composable
internal fun NoUpdateFoundDialogContent(
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  AktualAlertDialogContent(
    modifier = modifier,
    title = Strings.infoNoUpdateTitle,
    content = { Text(text = Strings.infoNoUpdateMessage) },
    buttons = {
      TextButton(onClick = onDismiss) {
        Text(text = Strings.infoNoUpdateOk, color = colors.pageTextPositive)
      }
    },
  )
}

@Preview
@Composable
private fun PreviewNoUpdatesContent(@PreviewParameter(ColoredParameters::class) colors: Colors) =
  PreviewWithColors(colors) { NoUpdateFoundDialogContent(onDismiss = {}) }
