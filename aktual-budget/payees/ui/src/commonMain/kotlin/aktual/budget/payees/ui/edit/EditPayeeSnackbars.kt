package aktual.budget.payees.ui.edit

import aktual.budget.model.PayeeLocationId
import aktual.budget.payees.vm.edit.EditPayeeEvent
import aktual.core.l10n.Res
import aktual.core.l10n.payees_deleted_undo
import aktual.core.l10n.payees_location_deleted
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import org.jetbrains.compose.resources.getString

internal suspend fun SnackbarHostState.showLocationDeleted(
  event: EditPayeeEvent.LocationDeleted,
  onUndo: (PayeeLocationId) -> Unit,
) {
  val result =
    showSnackbar(
      message = getString(Res.string.payees_location_deleted),
      actionLabel = getString(Res.string.payees_deleted_undo),
      duration = SnackbarDuration.Long,
    )
  if (result == ActionPerformed) {
    onUndo(event.id)
  }
}
