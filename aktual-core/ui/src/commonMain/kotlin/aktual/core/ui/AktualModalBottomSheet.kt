package aktual.core.ui

import aktual.core.icons.AktualIcons
import aktual.core.icons.Delete
import aktual.core.l10n.Strings
import aktual.core.ui.AktualTheme.colors
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity

/**
 * A wrapper around [ModalBottomSheet] that signals [DialogBlurState] so the background blur overlay
 * animates in while this sheet is showing. Pass [blurBackground] as false to keep the content
 * behind the sheet readable.
 */
@Composable
fun AktualModalBottomSheet(
  onDismissRequest: () -> Unit,
  modifier: Modifier = Modifier,
  sheetState: SheetState = rememberBottomSheetState(initialValue = Hidden),
  blurBackground: Boolean = true,
  content: @Composable ColumnScope.() -> Unit,
) {
  val dialogBlurState = LocalDialogBlurState.current
  DisposableEffect(blurBackground) {
    if (blurBackground) dialogBlurState.activeDialogCount++
    onDispose { if (blurBackground) dialogBlurState.activeDialogCount-- }
  }

  val density = LocalDensity.current
  val ime = WindowInsets.ime
  LaunchedEffect(sheetState) {
    snapshotFlow { ime.getBottom(density) > 0 }
      .collect { isVisible ->
        if (isVisible) {
          sheetState.expand()
        }
      }
  }

  ModalBottomSheet(
    modifier = modifier,
    onDismissRequest = onDismissRequest,
    sheetState = sheetState,
    containerColor = colors.modalBackground,
    contentColor = colors.pageText,
    dragHandle = {
      Box(modifier = Modifier.fillMaxWidth()) {
        BottomSheetDefaults.DragHandle(modifier = Modifier.align(Center))
        IconButton(modifier = Modifier.align(CenterEnd), onClick = onDismissRequest) {
          Icon(imageVector = AktualIcons.Delete, contentDescription = Strings.inputClose)
        }
      }
    },
    content = { ProvideSurfaceColor(colors.modalBackground) { content() } },
  )
}
