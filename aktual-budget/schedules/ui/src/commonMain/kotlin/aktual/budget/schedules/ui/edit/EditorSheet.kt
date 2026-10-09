package aktual.budget.schedules.ui.edit

import aktual.core.l10n.Strings
import aktual.core.ui.EditorSheet as CoreEditorSheet
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal fun EditorSheet(
  title: String,
  canConfirm: Boolean,
  onDismiss: () -> Unit,
  onConfirm: () -> Unit,
  modifier: Modifier = Modifier,
  content: @Composable ColumnScope.() -> Unit,
) =
  CoreEditorSheet(
    modifier = modifier,
    title = title,
    cancelText = Strings.editScheduleDatePickerCancel,
    confirmText = Strings.editScheduleDone,
    canConfirm = canConfirm,
    onDismiss = onDismiss,
    onConfirm = onConfirm,
    content = { content() },
  )
