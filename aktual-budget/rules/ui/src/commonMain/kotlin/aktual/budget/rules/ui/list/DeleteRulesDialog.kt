package aktual.budget.rules.ui.list

import aktual.core.l10n.Strings
import aktual.core.theme.Colors
import aktual.core.ui.AktualAlertDialog
import aktual.core.ui.AktualAlertDialogContent
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.ColorParameters
import aktual.core.ui.DialogTextButton
import aktual.core.ui.PreviewWithColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter

@Composable
internal fun DeleteRulesDialog(
  onConfirm: () -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  AktualAlertDialog(
    modifier = modifier,
    onDismissRequest = onDismiss,
    content = { DeleteRulesDialogContent(onConfirm, onDismiss) },
  )
}

@Composable
private fun DeleteRulesDialogContent(
  onConfirm: () -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  AktualAlertDialogContent(
    modifier = modifier,
    title = Strings.rulesDeleteDialogTitle,
    highlight = colors.errorText,
    content = { Text(text = Strings.rulesDeleteDialogMessage) },
    buttons = {
      DialogTextButton(text = Strings.rulesDeleteDialogCancel, onClick = onDismiss)
      DialogTextButton(
        kind = Destructive,
        text = Strings.rulesDeleteDialogConfirm,
        onClick = onConfirm,
      )
    },
  )
}

@Preview
@Composable
private fun PreviewDeleteRulesDialog(@PreviewParameter(ColorParameters::class) colors: Colors) =
  PreviewWithColors(colors) { DeleteRulesDialogContent(onConfirm = {}, onDismiss = {}) }
