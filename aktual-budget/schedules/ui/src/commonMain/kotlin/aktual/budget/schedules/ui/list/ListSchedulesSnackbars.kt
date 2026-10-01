package aktual.budget.schedules.ui.list

import aktual.budget.schedules.vm.Schedule
import aktual.budget.schedules.vm.list.ListSchedulesEvent
import aktual.core.l10n.Res
import aktual.core.l10n.list_schedules_delete_failed
import aktual.core.l10n.list_schedules_deleted
import aktual.core.l10n.list_schedules_deleted_undo
import aktual.core.l10n.list_schedules_post_unsupported
import aktual.core.l10n.list_schedules_restore_failed
import aktual.core.l10n.list_schedules_unnamed_schedule
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import org.jetbrains.compose.resources.getString

internal suspend fun SnackbarHostState.showDeleted(
  event: ListSchedulesEvent.Deleted,
  onUndo: (Schedule, Int) -> Unit,
) {
  val result =
    showSnackbar(
      message = getString(Res.string.list_schedules_deleted, event.schedule.label()),
      actionLabel = getString(Res.string.list_schedules_deleted_undo),
      duration = SnackbarDuration.Long,
    )
  if (result == ActionPerformed) {
    onUndo(event.schedule, event.index)
  }
}

internal suspend fun SnackbarHostState.showDeleteFailed(event: ListSchedulesEvent.DeleteFailed) {
  showSnackbar(getString(Res.string.list_schedules_delete_failed, event.schedule.label()))
}

internal suspend fun SnackbarHostState.showRestoreFailed(event: ListSchedulesEvent.RestoreFailed) {
  showSnackbar(getString(Res.string.list_schedules_restore_failed, event.schedule.label()))
}

// TODO: post the transaction once there's a transaction writer, see #1649
internal suspend fun SnackbarHostState.showPostUnsupported() {
  showSnackbar(getString(Res.string.list_schedules_post_unsupported))
}

private suspend fun Schedule.label(): String =
  name ?: getString(Res.string.list_schedules_unnamed_schedule)
