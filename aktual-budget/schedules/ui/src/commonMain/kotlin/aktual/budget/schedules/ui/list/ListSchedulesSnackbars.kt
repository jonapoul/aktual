package aktual.budget.schedules.ui.list

import aktual.budget.schedules.domain.Schedule
import aktual.budget.schedules.vm.list.ListSchedulesEvent
import aktual.core.l10n.Res
import aktual.core.l10n.list_schedules_complete_failed
import aktual.core.l10n.list_schedules_completed
import aktual.core.l10n.list_schedules_delete_failed
import aktual.core.l10n.list_schedules_deleted
import aktual.core.l10n.list_schedules_deleted_undo
import aktual.core.l10n.list_schedules_post_failed
import aktual.core.l10n.list_schedules_posted
import aktual.core.l10n.list_schedules_restart_failed
import aktual.core.l10n.list_schedules_restore_failed
import aktual.core.l10n.list_schedules_unnamed_schedule
import aktual.core.l10n.list_schedules_unpost_failed
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import org.jetbrains.compose.resources.getString

internal suspend fun SnackbarHostState.showDeleted(
  event: ListSchedulesEvent.Deleted,
  onUndo: (Schedule) -> Unit,
) {
  val result =
    showSnackbar(
      message = getString(Res.string.list_schedules_deleted, event.schedule.label()),
      actionLabel = getString(Res.string.list_schedules_deleted_undo),
      duration = SnackbarDuration.Long,
    )
  if (result == ActionPerformed) {
    onUndo(event.schedule)
  }
}

internal suspend fun SnackbarHostState.showDeleteFailed(event: ListSchedulesEvent.DeleteFailed) {
  showSnackbar(getString(Res.string.list_schedules_delete_failed, event.schedule.label()))
}

internal suspend fun SnackbarHostState.showRestoreFailed(event: ListSchedulesEvent.RestoreFailed) {
  showSnackbar(getString(Res.string.list_schedules_restore_failed, event.schedule.label()))
}

internal suspend fun SnackbarHostState.showCompleted(
  event: ListSchedulesEvent.Completed,
  onUndo: (Schedule) -> Unit,
) {
  val result =
    showSnackbar(
      message = getString(Res.string.list_schedules_completed, event.schedule.label()),
      actionLabel = getString(Res.string.list_schedules_deleted_undo),
      duration = SnackbarDuration.Long,
    )
  if (result == ActionPerformed) {
    onUndo(event.schedule)
  }
}

internal suspend fun SnackbarHostState.showCompleteFailed(
  event: ListSchedulesEvent.CompleteFailed,
) {
  showSnackbar(getString(Res.string.list_schedules_complete_failed, event.schedule.label()))
}

internal suspend fun SnackbarHostState.showRestartFailed(event: ListSchedulesEvent.RestartFailed) {
  showSnackbar(getString(Res.string.list_schedules_restart_failed, event.schedule.label()))
}

internal suspend fun SnackbarHostState.showPosted(
  event: ListSchedulesEvent.Posted,
  onUndo: (ListSchedulesEvent.Posted) -> Unit,
) {
  val result =
    showSnackbar(
      message = getString(Res.string.list_schedules_posted, event.schedule.label()),
      actionLabel =
        getString(Res.string.list_schedules_deleted_undo).takeIf {
          event.transactions.isNotEmpty()
        },
      duration = SnackbarDuration.Long,
    )
  if (result == ActionPerformed) {
    onUndo(event)
  }
}

internal suspend fun SnackbarHostState.showPostFailed(event: ListSchedulesEvent.PostFailed) {
  showSnackbar(getString(Res.string.list_schedules_post_failed, event.schedule.label()))
}

internal suspend fun SnackbarHostState.showUnpostFailed(event: ListSchedulesEvent.UnpostFailed) {
  showSnackbar(getString(Res.string.list_schedules_unpost_failed, event.schedule.label()))
}

private suspend fun Schedule.label(): String =
  name ?: getString(Res.string.list_schedules_unnamed_schedule)
