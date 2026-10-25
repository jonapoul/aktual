package aktual.budget.payees.ui.list

import aktual.budget.model.PayeeId
import aktual.budget.payees.vm.list.ListPayeesEvent
import aktual.core.l10n.Res
import aktual.core.l10n.payees_delete_failed
import aktual.core.l10n.payees_deleted
import aktual.core.l10n.payees_deleted_undo
import aktual.core.l10n.payees_restore_failed
import aktual.core.l10n.payees_update_failed
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import org.jetbrains.compose.resources.getPluralString
import org.jetbrains.compose.resources.getString

internal suspend fun SnackbarHostState.showDeleted(
  event: ListPayeesEvent.Deleted,
  onUndo: (Set<PayeeId>) -> Unit,
) {
  val count = event.ids.size
  val result =
    showSnackbar(
      message = getPluralString(Res.plurals.payees_deleted, count, count),
      actionLabel = getString(Res.string.payees_deleted_undo),
      duration = SnackbarDuration.Long,
    )
  if (result == ActionPerformed) {
    onUndo(event.ids)
  }
}

internal suspend fun SnackbarHostState.showFailure(event: ListPayeesEvent) {
  val message =
    when (event) {
      is Deleted -> return
      DeleteFailed -> Res.string.payees_delete_failed
      RestoreFailed -> Res.string.payees_restore_failed
      UpdateFailed -> Res.string.payees_update_failed
    }
  showSnackbar(getString(message))
}
