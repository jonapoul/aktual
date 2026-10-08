package aktual.core.ui

import aktual.core.ui.AktualTheme.colors
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier

/**
 * A wrapper around [ModalBottomSheet] that signals [DialogBlurState] so the background blur overlay
 * animates in while this sheet is showing.
 */
@Composable
fun AktualModalBottomSheet(
  onDismissRequest: () -> Unit,
  sheetState: SheetState,
  modifier: Modifier = Modifier,
  content: @Composable ColumnScope.() -> Unit,
) {
  val dialogBlurState = LocalDialogBlurState.current
  DisposableEffect(Unit) {
    dialogBlurState.activeDialogCount++
    onDispose { dialogBlurState.activeDialogCount-- }
  }

  ModalBottomSheet(
    modifier = modifier,
    onDismissRequest = onDismissRequest,
    sheetState = sheetState,
    containerColor = colors.modalBackground,
    contentColor = colors.pageText,
    content = content,
  )
}
