package aktual.budget.reports.ui.choosetype

import aktual.budget.reports.vm.choosetype.ChooseReportTypeDialog
import aktual.core.l10n.Strings
import aktual.core.ui.AktualAlertDialog
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.DialogTextButton
import aktual.core.ui.OnAction
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
internal fun ChooseReportTypeDialogs(
  dialog: ChooseReportTypeDialog?,
  onAction: OnAction<ChooseReportTypeAction>,
) {
  when (dialog) {
    null -> return
    UnsupportedType -> UnsupportedTypeDialog(onAction)
  }
}

@Composable
private fun UnsupportedTypeDialog(onAction: OnAction<ChooseReportTypeAction>) {
  AktualAlertDialog(
    title = Strings.reportsChooseTypeDisabledDialogTitle,
    highlight = colors.warningText,
    onDismissRequest = { onAction(DismissDialog) },
    buttons = {
      DialogTextButton(
        text = Strings.reportsChooseTypeDisabledDialogDismiss,
        onClick = { onAction(DismissDialog) },
      )
    },
    content = { Text(Strings.reportsChooseTypeDisabled) },
  )
}
