package aktual.account.ui.login

import aktual.core.l10n.Strings
import aktual.core.ui.AktualAlertDialog
import aktual.core.ui.AktualAlertDialogContent
import aktual.core.ui.ColorParameters
import aktual.core.ui.DialogTextButton
import aktual.core.ui.PreviewWithColors
import aktual.theme.Colors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter

@Composable
internal fun SessionEndedDialog(
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  AktualAlertDialog(
    modifier = modifier,
    onDismissRequest = onDismiss,
    content = { SessionEndedDialogContent(onDismiss = onDismiss) },
  )
}

@Composable
private fun SessionEndedDialogContent(
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  AktualAlertDialogContent(
    modifier = modifier,
    title = Strings.loginSessionEndedTitle,
    content = { Text(text = Strings.loginSessionEndedMessage) },
    buttons = {
      DialogTextButton(text = Strings.loginSessionEndedOk, onClick = onDismiss)
    },
  )
}

@Preview
@Composable
private fun PreviewSessionEndedDialog(@PreviewParameter(ColorParameters::class) colors: Colors) =
  PreviewWithColors(colors) { SessionEndedDialogContent(onDismiss = {}) }
