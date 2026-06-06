package aktual.budget.reports.ui.choosetype

import aktual.budget.reports.vm.choosetype.ChooseReportTypeDialog
import aktual.core.l10n.Strings
import aktual.core.ui.AktualAlertDialog
import aktual.core.ui.AktualTheme.colors
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
internal fun ChooseReportTypeDialogs(
  dialog: ChooseReportTypeDialog?,
  onAction: ChooseReportTypeActionHandler,
) {
  when (dialog) {
    null -> return
    ChooseReportTypeDialog.UnsupportedType -> UnsupportedTypeDialog(onAction)
  }
}

@Composable
private fun UnsupportedTypeDialog(onAction: ChooseReportTypeActionHandler) {
  AktualAlertDialog(
    title = Strings.reportsChooseTypeDisabledDialogTitle,
    titleColor = colors.warningText,
    onDismissRequest = { onAction(DismissDialog) },
    buttons = {
      TextButton(onClick = { onAction(DismissDialog) }) {
        Text(Strings.reportsChooseTypeDisabledDialogDismiss)
      }
    },
    content = { Text(Strings.reportsChooseTypeDisabled) },
  )
}
