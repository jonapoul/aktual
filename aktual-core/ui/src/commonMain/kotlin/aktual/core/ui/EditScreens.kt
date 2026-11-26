package aktual.core.ui

import aktual.core.icons.AktualIcons
import aktual.core.icons.Check
import aktual.core.icons.Delete
import aktual.core.ui.AktualTheme.colors
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

// Red toolbar button replacing the back button once an edit screen has unsaved changes
@Composable
fun DiscardChangesButton(
  contentDescription: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  BasicIconButton(
    modifier = modifier,
    imageVector = AktualIcons.Delete,
    contentDescription = contentDescription,
    colors = DiscardColors,
    onClick = onClick,
  )
}

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

@Composable
fun SaveFab(
  isVisible: Boolean,
  text: String,
  isEnabled: Boolean,
  isLoading: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  isExpanded: Boolean = true,
) {
  AktualFab(
    modifier = modifier.padding(end = Dimens.Huge, bottom = Dimens.Huge),
    text = text,
    icon = AktualIcons.Check,
    isVisible = isVisible,
    isExpanded = isExpanded,
    isEnabled = isEnabled,
    isLoading = isLoading,
    onClick = onClick,
  )
}

private val DiscardColors = IconButtonColorProvider { colors, isPressed ->
  colors.bareIconButton(isPressed).copy(contentColor = colors.errorText)
}
