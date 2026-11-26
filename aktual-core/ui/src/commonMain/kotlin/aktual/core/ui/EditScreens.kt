package aktual.core.ui

import aktual.core.icons.AktualIcons
import aktual.core.icons.Check
import aktual.core.icons.Delete
import aktual.core.ui.AktualTheme.colors
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.BottomEnd
import androidx.compose.ui.Alignment.Companion.BottomStart
import androidx.compose.ui.Modifier

@Composable
fun DiscardChangesDialog(
  title: String,
  message: String,
  confirm: String,
  cancel: String,
  onDiscard: () -> Unit,
  onCancel: () -> Unit,
  modifier: Modifier = Modifier,
) {
  AktualAlertDialog(
    modifier = modifier,
    title = title,
    highlight = colors.warningText,
    onDismissRequest = onCancel,
    buttons = {
      DialogTextButton(text = cancel, onClick = onCancel)
      DialogTextButton(kind = Destructive, text = confirm, onClick = onDiscard)
    },
    content = { Text(message) },
  )
}

/**
 * Save and discard FABs along the bottom of an edit screen: an icon-only discard at the start and
 * save at the end. Both collapse and fade while the content scrolls down, like [CornerFabs]. Pass
 * the screen's own strings.
 */
@Composable
fun EditFabs(
  saveText: String,
  discardText: String,
  onSave: () -> Unit,
  onDiscard: () -> Unit,
  modifier: Modifier = Modifier,
  isSaveVisible: Boolean = true,
  isDiscardVisible: Boolean = isSaveVisible,
  canSave: Boolean = true,
  isSaving: Boolean = false,
  isExpanded: Boolean = true,
  saveModifier: Modifier = Modifier,
  discardModifier: Modifier = Modifier,
) {
  Box(
    modifier =
      modifier.fillMaxWidth().padding(start = Dimens.Huge, end = Dimens.Huge, bottom = Dimens.Huge),
  ) {
    AktualFab(
      modifier = discardModifier.align(BottomStart),
      text = discardText,
      icon = AktualIcons.Delete,
      kind = Destructive,
      isVisible = isDiscardVisible,
      isExpanded = false,
      isFaded = !isExpanded,
      isEnabled = !isSaving,
      onClick = onDiscard,
    )

    AktualFab(
      modifier = saveModifier.align(BottomEnd),
      text = saveText,
      icon = AktualIcons.Check,
      isVisible = isSaveVisible,
      isExpanded = isExpanded,
      isEnabled = canSave,
      isLoading = isSaving,
      onClick = onSave,
    )
  }
}
