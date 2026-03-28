package aktual.budget.reports.ui.choosetype

import aktual.budget.reports.vm.choosetype.ChooseReportTypeDialog
import aktual.core.l10n.Strings
import aktual.core.theme.LocalTheme
import aktual.core.theme.Theme
import aktual.core.ui.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
internal fun ChooseReportTypeDialogs(
  dialog: ChooseReportTypeDialog?,
  onAction: (ChooseReportTypeAction) -> Unit,
) {
  when (dialog) {
    null -> return
    ChooseReportTypeDialog.UnsupportedType -> UnsupportedTypeDialog(onAction)
  }
}

@Composable
private fun UnsupportedTypeDialog(
  onAction: (ChooseReportTypeAction) -> Unit,
  theme: Theme = LocalTheme.current,
) {
  AlertDialog(
    title = Strings.reportsChooseTypeDisabledDialogTitle,
    titleColor = theme.warningText,
    onDismissRequest = { onAction(DismissDialog) },
    buttons = {
      TextButton(onClick = { onAction(DismissDialog) }) {
        Text(Strings.reportsChooseTypeDisabledDialogDismiss)
      }
    },
    content = { Text(Strings.reportsChooseTypeDisabled) },
  )
}
