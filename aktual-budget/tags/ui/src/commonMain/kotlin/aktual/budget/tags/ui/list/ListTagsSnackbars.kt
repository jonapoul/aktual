package aktual.budget.tags.ui.list

import aktual.budget.tags.vm.list.ListTagsEvent
import aktual.budget.tags.vm.list.TagItem
import aktual.core.l10n.Res
import aktual.core.l10n.tags_delete_failed
import aktual.core.l10n.tags_delete_failed_unknown
import aktual.core.l10n.tags_deleted
import aktual.core.l10n.tags_deleted_undo
import aktual.core.l10n.tags_restore_failed
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import org.jetbrains.compose.resources.getString

// shows the delete confirmation with an undo action, invoking onUndo if the user taps it
internal suspend fun SnackbarHostState.showDeleted(
  event: ListTagsEvent.Deleted,
  onUndo: (TagItem, Int) -> Unit,
) {
  val result =
    showSnackbar(
      message = getString(Res.string.tags_deleted, event.item.tag),
      actionLabel = getString(Res.string.tags_deleted_undo),
      duration = SnackbarDuration.Long,
    )
  if (result == SnackbarResult.ActionPerformed) {
    onUndo(event.item, event.index)
  }
}

internal suspend fun SnackbarHostState.showDeleteFailed(event: ListTagsEvent.DeleteFailed) {
  showSnackbar(
    event.tag?.let { getString(Res.string.tags_delete_failed, it) }
      ?: getString(Res.string.tags_delete_failed_unknown)
  )
}

internal suspend fun SnackbarHostState.showRestoreFailed(event: ListTagsEvent.RestoreFailed) {
  showSnackbar(getString(Res.string.tags_restore_failed, event.tag))
}
