package aktual.budget.accounts.components

import aktual.budget.accounts.domain.AccountUndo
import aktual.budget.accounts.vm.edit.EditAccountEvent
import aktual.core.l10n.Res
import aktual.core.l10n.accounts_edit_renamed
import aktual.core.l10n.accounts_edit_saved
import aktual.core.l10n.accounts_manage_undo
import aktual.core.ui.showSuccessSnackbar
import androidx.compose.material3.SnackbarDuration.Long
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SnackbarResult.ActionPerformed
import org.jetbrains.compose.resources.getString

suspend fun SnackbarHostState.showUndoable(message: String): SnackbarResult =
  showSuccessSnackbar(
    message = message,
    actionLabel = getString(Res.string.accounts_manage_undo),
    duration = Long,
  )

internal suspend fun SnackbarHostState.showSaved(
  event: EditAccountEvent.Saved,
  undo: (AccountUndo) -> Unit,
) {
  val resource =
    if (event.renamedFrom == null) {
      Res.string.accounts_edit_saved
    } else {
      Res.string.accounts_edit_renamed
    }
  if (showUndoable(getString(resource, event.name)) == ActionPerformed) undo(event.undo)
}
