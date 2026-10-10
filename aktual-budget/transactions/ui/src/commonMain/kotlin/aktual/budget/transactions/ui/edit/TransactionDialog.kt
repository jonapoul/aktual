package aktual.budget.transactions.ui.edit

import aktual.budget.transactions.vm.edit.EditTransactionError
import aktual.core.l10n.Strings
import aktual.core.ui.AktualAlertDialog
import aktual.core.ui.AktualTheme.colors
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

internal enum class TransactionDialog {
  ConfirmDelete,
  ConfirmDiscard,
  // As upstream's confirm-transaction-edit modal, shown before changing a reconciled transaction
  ConfirmReconciledSave,
  ConfirmReconciledUnlock,
  ConfirmReconciledDelete,
}

@Composable
internal fun EditTransactionDialogs(
  dialog: TransactionDialog?,
  onAction: EditTransactionActionHandler,
  onShow: (TransactionDialog?) -> Unit,
) {
  val dismiss = { onShow(null) }
  when (dialog) {
    ConfirmDelete ->
      ConfirmDialog(
        title = Strings.transactionDeleteTitle,
        message = Strings.transactionDeleteMessage,
        confirm = Strings.transactionDeleteConfirm,
        cancel = Strings.transactionSheetCancel,
        highlight = colors.errorText,
        onConfirm = {
          dismiss()
          onAction(DeleteTransaction)
        },
        onCancel = dismiss,
      )

    ConfirmDiscard ->
      ConfirmDialog(
        title = Strings.transactionDiscardTitle,
        message = Strings.transactionDiscardMessage,
        confirm = Strings.transactionDiscardConfirm,
        cancel = Strings.transactionDiscardCancel,
        highlight = colors.warningText,
        onConfirm = {
          dismiss()
          onAction(StopEditing)
        },
        onCancel = dismiss,
      )

    ConfirmReconciledSave ->
      ReconciledDialog(
        message = Strings.transactionReconciledSave,
        onConfirm = {
          dismiss()
          onAction(SaveTransaction)
        },
        onCancel = dismiss,
      )

    ConfirmReconciledUnlock ->
      ReconciledDialog(
        message = Strings.transactionReconciledUnlock,
        onConfirm = {
          dismiss()
          onAction(UnlockReconciled)
        },
        onCancel = dismiss,
      )

    // Upstream then asks whether to delete, as for any other transaction
    ConfirmReconciledDelete ->
      ReconciledDialog(
        message = Strings.transactionReconciledDelete,
        onConfirm = { onShow(ConfirmDelete) },
        onCancel = dismiss,
      )

    null -> Unit
  }
}

@Composable
private fun ReconciledDialog(message: String, onConfirm: () -> Unit, onCancel: () -> Unit) =
  ConfirmDialog(
    title = Strings.transactionReconciledTitle,
    message = message,
    confirm = Strings.transactionReconciledConfirm,
    cancel = Strings.transactionSheetCancel,
    highlight = colors.warningText,
    onConfirm = onConfirm,
    onCancel = onCancel,
  )

@Composable
private fun ConfirmDialog(
  title: String,
  message: String,
  confirm: String,
  cancel: String,
  highlight: Color,
  onConfirm: () -> Unit,
  onCancel: () -> Unit,
) =
  AktualAlertDialog(
    title = title,
    highlight = highlight,
    onDismissRequest = onCancel,
    buttons = {
      TextButton(onClick = onCancel) { Text(cancel) }
      TextButton(onClick = onConfirm) { Text(confirm, color = highlight) }
    },
    content = { Text(message) },
  )

@Composable
internal fun EditTransactionErrorDialog(error: EditTransactionError, onDismiss: () -> Unit) =
  AktualAlertDialog(
    title =
      when (error) {
        is Saving -> Strings.transactionErrorSaving
        is Deleting -> Strings.transactionErrorDeleting
      },
    highlight = colors.errorText,
    onDismissRequest = onDismiss,
    buttons = { TextButton(onClick = onDismiss) { Text(Strings.transactionErrorDismiss) } },
    content = {
      Text(
        when (error) {
          is Saving -> error.reason
          is Deleting -> error.reason
        },
      )
    },
  )
