package aktual.budget.transactions.ui.edit

import aktual.budget.transactions.vm.edit.EditTransactionError
import aktual.budget.transactions.vm.edit.UnusedPayees
import aktual.core.l10n.Strings
import aktual.core.ui.AktualAlertDialog
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.DialogButtonKind
import aktual.core.ui.DialogTextButton
import aktual.core.ui.Dimens
import aktual.core.ui.OnAction
import aktual.core.ui.switch
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.util.fastForEach

internal enum class TransactionDialog {
  ConfirmDelete,
  ConfirmDiscard,
  // Shown before saving over changes made elsewhere since editing started
  ConfirmOverwrite,
  // As upstream's confirm-transaction-edit modal, shown before changing a reconciled transaction
  ConfirmReconciledSave,
  ConfirmReconciledUnlock,
  ConfirmReconciledDelete,
  // The same, for a transfer whose other side is the reconciled one
  ConfirmReconciledTransferSave,
  ConfirmReconciledTransferDelete,
}

@Composable
internal fun EditTransactionDialogs(
  dialog: TransactionDialog?,
  onAction: OnAction<EditTransactionAction>,
  onShow: (TransactionDialog?) -> Unit,
  afterOverwrite: () -> TransactionDialog? = { null },
) {
  val dismiss = { onShow(null) }
  when (dialog) {
    ConfirmDelete ->
      ConfirmDeleteDialog(
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
          onAction(DiscardChanges)
        },
        onCancel = dismiss,
      )

    ConfirmOverwrite ->
      ConfirmDialog(
        title = Strings.transactionOverwriteTitle,
        message = Strings.transactionOverwriteMessage,
        confirm = Strings.transactionOverwriteConfirm,
        cancel = Strings.transactionSheetCancel,
        highlight = colors.warningText,
        onConfirm = {
          val next = afterOverwrite()
          if (next == null) {
            dismiss()
            onAction(SaveTransaction)
          } else {
            onShow(next)
          }
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

    ConfirmReconciledTransferSave ->
      ReconciledDialog(
        message = Strings.transactionReconciledTransferSave,
        onConfirm = {
          dismiss()
          onAction(SaveTransaction)
        },
        onCancel = dismiss,
      )

    ConfirmReconciledTransferDelete ->
      ReconciledDialog(
        message = Strings.transactionReconciledTransferDelete,
        onConfirm = { onShow(ConfirmDelete) },
        onCancel = dismiss,
      )

    null -> Unit
  }
}

@Composable
internal fun ConfirmDeleteDialog(onConfirm: () -> Unit, onCancel: () -> Unit) =
  ConfirmDialog(
    title = Strings.transactionDeleteTitle,
    message = Strings.transactionDeleteMessage,
    confirm = Strings.transactionDeleteConfirm,
    cancel = Strings.transactionSheetCancel,
    highlight = colors.errorText,
    confirmKind = Destructive,
    onConfirm = onConfirm,
    onCancel = onCancel,
  )

@Composable
internal fun ReconciledDialog(message: String, onConfirm: () -> Unit, onCancel: () -> Unit) =
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
  confirmKind: DialogButtonKind = Primary,
) =
  AktualAlertDialog(
    title = title,
    highlight = highlight,
    onDismissRequest = onCancel,
    buttons = {
      DialogTextButton(text = cancel, onClick = onCancel)
      DialogTextButton(kind = confirmKind, text = confirm, onClick = onConfirm)
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
        is CreatingTag -> Strings.transactionErrorCreatingTag
      },
    highlight = colors.errorText,
    onDismissRequest = onDismiss,
    buttons = { DialogTextButton(text = Strings.transactionErrorDismiss, onClick = onDismiss) },
    content = {
      Text(
        when (error) {
          is Saving -> error.reason
          is Deleting -> error.reason
          is CreatingTag -> error.reason
        },
      )
    },
  )

// As upstream's MergeUnusedPayeesModal, without its "Merge and edit rule" button
@Composable
internal fun UnusedPayeesDialog(
  unused: UnusedPayees,
  isWorking: Boolean,
  onMerge: (addRenameRule: Boolean) -> Unit,
  onKeep: () -> Unit,
) {
  var addRenameRule by rememberSaveable { mutableStateOf(true) }
  val single = unused.payees.singleOrNull()
  val title =
    if (single != null) {
      Strings.transactionUnusedPayeeTitle
    } else {
      Strings.transactionUnusedPayeesTitle
    }
  AktualAlertDialog(
    title = title,
    onDismissRequest = { if (!isWorking) onKeep() },
    buttons = {
      DialogTextButton(
        text = Strings.transactionUnusedPayeesKeep,
        isEnabled = !isWorking,
        onClick = onKeep,
      )
      DialogTextButton(
        kind = Primary,
        text = Strings.transactionUnusedPayeesMerge,
        isEnabled = !isWorking,
        isLoading = isWorking,
        onClick = { onMerge(addRenameRule) },
      )
    },
    content = {
      Column(verticalArrangement = spacedBy(Dimens.Large)) {
        if (single != null) {
          Text(Strings.transactionUnusedPayeeMessage(single.name, unused.target.name))
        } else {
          Text(Strings.transactionUnusedPayeesMessage(unused.target.name))
          Column {
            unused.payees.fastForEach { payee ->
              Text(text = payee.name, color = colors.pageTextPositive)
            }
          }
        }
        Text(
          text = Strings.transactionUnusedPayeesInfo,
          style = typography.bodySmall,
          color = colors.pageTextSubdued,
        )
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = CenterVertically,
          horizontalArrangement = spacedBy(Dimens.Large),
        ) {
          Text(
            modifier = Modifier.weight(1f),
            text = Strings.transactionUnusedPayeesRename,
            style = typography.bodyMedium,
          )
          Switch(
            checked = addRenameRule,
            onCheckedChange = { addRenameRule = it },
            enabled = !isWorking,
            colors = colors.switch(),
          )
        }
      }
    },
  )
}
