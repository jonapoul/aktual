package aktual.core.ui

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.DatePickerDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.DialogProperties

/**
 * A wrapper around [DatePickerDialog] that signals [DialogBlurState] so the background blur overlay
 * animates in while this dialog is showing.
 */
@Composable
fun AktualDatePickerDialog(
  onDismissRequest: () -> Unit,
  confirmButton: @Composable () -> Unit,
  modifier: Modifier = Modifier,
  dismissButton: @Composable (() -> Unit)? = null,
  properties: DialogProperties = DialogProperties(usePlatformDefaultWidth = false),
  content: @Composable ColumnScope.() -> Unit,
) {
  val dialogBlurState = LocalDialogBlurState.current
  DisposableEffect(Unit) {
    dialogBlurState.activeDialogCount++
    onDispose { dialogBlurState.activeDialogCount-- }
  }

  DatePickerDialog(
    onDismissRequest = onDismissRequest,
    confirmButton = confirmButton,
    modifier = modifier,
    dismissButton = dismissButton,
    properties = properties,
    content = content,
  )
}
