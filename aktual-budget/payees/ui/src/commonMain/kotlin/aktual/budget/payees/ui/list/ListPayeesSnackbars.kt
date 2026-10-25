package aktual.budget.payees.ui.list

import aktual.budget.payees.vm.list.ListPayeesEvent
import aktual.budget.payees.vm.list.PayeeItem
import aktual.core.l10n.Res
import aktual.core.l10n.payees_delete_failed
import aktual.core.l10n.payees_deleted
import aktual.core.l10n.payees_deleted_undo
import aktual.core.l10n.payees_merge_failed
import aktual.core.l10n.payees_merged
import aktual.core.l10n.payees_restore_failed
import aktual.core.l10n.payees_update_failed
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import org.jetbrains.compose.resources.getString

internal suspend fun SnackbarHostState.showDeleted(
  event: ListPayeesEvent.Deleted,
  onUndo: (PayeeItem) -> Unit,
) {
  val result =
    showSnackbar(
      message = getString(Res.string.payees_deleted, event.payee.name),
      actionLabel = getString(Res.string.payees_deleted_undo),
      duration = SnackbarDuration.Long,
    )
  if (result == ActionPerformed) {
    onUndo(event.payee)
  }
}

internal suspend fun SnackbarHostState.showMerged(
  event: ListPayeesEvent.Merged,
  onUndo: (ListPayeesEvent.Merged) -> Unit,
) {
  val result =
    showSnackbar(
      message = getString(Res.string.payees_merged, event.payee.name, event.target.name),
      actionLabel = getString(Res.string.payees_deleted_undo),
      duration = SnackbarDuration.Long,
    )
  if (result == ActionPerformed) {
    onUndo(event)
  }
}

internal suspend fun SnackbarHostState.showFailure(event: ListPayeesEvent) {
  val message =
    when (event) {
      is Deleted,
      is Merged -> return
      DeleteFailed -> Res.string.payees_delete_failed
      MergeFailed -> Res.string.payees_merge_failed
      RestoreFailed -> Res.string.payees_restore_failed
      UpdateFailed -> Res.string.payees_update_failed
    }
  showSnackbar(getString(message))
}
