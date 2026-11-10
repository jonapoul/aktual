package aktual.budget.accounts.ui.manage

import aktual.budget.accounts.components.showUndoable
import aktual.budget.accounts.domain.AccountUndo
import aktual.budget.accounts.vm.close.CloseAccountEvent
import aktual.budget.accounts.vm.manage.ManageAccountsEvent
import aktual.core.l10n.Res
import aktual.core.l10n.accounts_close_closed
import aktual.core.l10n.accounts_close_deleted
import aktual.core.l10n.accounts_close_unlink_failed
import aktual.core.l10n.accounts_delete_unlink_failed
import aktual.core.l10n.accounts_manage_action_failed
import aktual.core.l10n.accounts_manage_group_moved
import aktual.core.l10n.accounts_manage_group_name_taken
import aktual.core.l10n.accounts_manage_moved
import aktual.core.l10n.accounts_manage_reopened
import aktual.core.l10n.accounts_manage_undo
import aktual.core.l10n.accounts_manage_ungrouped
import aktual.core.ui.showErrorSnackbar
import aktual.core.ui.showSuccessSnackbar
import aktual.core.ui.showWarningSnackbar
import androidx.compose.material3.SnackbarDuration.Long
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult.ActionPerformed
import org.jetbrains.compose.resources.getString

internal suspend fun SnackbarHostState.showAccountMoved(
  event: ManageAccountsEvent.AccountMoved,
  undo: (AccountUndo) -> Unit,
) {
  val group = event.group ?: getString(Res.string.accounts_manage_ungrouped)
  val message = getString(Res.string.accounts_manage_moved, event.name, group)
  if (showUndoable(message) == ActionPerformed) undo(event.undo)
}

internal suspend fun SnackbarHostState.showGroupMoved(
  event: ManageAccountsEvent.GroupMoved,
  undo: (AccountUndo) -> Unit,
) {
  val message = getString(Res.string.accounts_manage_group_moved, event.name)
  if (showUndoable(message) == ActionPerformed) undo(event.undo)
}

internal suspend fun SnackbarHostState.showReopened(event: ManageAccountsEvent.Reopened) {
  showSuccessSnackbar(getString(Res.string.accounts_manage_reopened, event.name))
}

internal suspend fun SnackbarHostState.showGroupNameTaken(
  event: ManageAccountsEvent.GroupNameTaken,
) {
  showErrorSnackbar(getString(Res.string.accounts_manage_group_name_taken, event.name))
}

internal suspend fun SnackbarHostState.showActionFailed() {
  showErrorSnackbar(getString(Res.string.accounts_manage_action_failed))
}

internal suspend fun SnackbarHostState.showClosed(
  event: CloseAccountEvent.Closed,
  undo: (AccountUndo) -> Unit,
) {
  val result =
    if (event.unlinkFailed) {
      showWarningSnackbar(
        message = getString(Res.string.accounts_close_unlink_failed, event.name),
        actionLabel = getString(Res.string.accounts_manage_undo),
        duration = Long,
      )
    } else {
      val resource =
        if (event.deleted) Res.string.accounts_close_deleted else Res.string.accounts_close_closed
      showUndoable(getString(resource, event.name))
    }
  if (result == ActionPerformed) undo(event.undo)
}

internal suspend fun SnackbarHostState.showDeleted(event: CloseAccountEvent.Deleted) {
  if (event.unlinkFailed) {
    val message = getString(Res.string.accounts_delete_unlink_failed, event.name)
    showWarningSnackbar(message, duration = Long)
  } else {
    showSuccessSnackbar(getString(Res.string.accounts_close_deleted, event.name))
  }
}
