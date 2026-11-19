package aktual.core.ui

import aktual.core.icons.AktualIcons
import aktual.core.icons.Check
import aktual.core.icons.Delete
import aktual.core.ui.AktualTheme.colors
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Room at the end of scrolling content so the last item can scroll out from under a FAB
val FabClearance = 72.dp

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
) {
  AnimatedVisibility(
    modifier = modifier,
    visible = isVisible,
    enter = slideInVertically { it } + scaleIn(initialScale = FAB_INITIAL_SCALE) + fadeIn(),
    exit = slideOutVertically { it } + scaleOut(targetScale = FAB_INITIAL_SCALE) + fadeOut(),
  ) {
    AktualButton(
      modifier = Modifier.padding(end = Dimens.Huge, bottom = Dimens.Huge),
      text = text,
      icon = AktualIcons.Check,
      kind = Primary,
      shape = CardShape,
      contentPadding = SaveFabPadding,
      isEnabled = isEnabled,
      isLoading = isLoading,
      onClick = onClick,
    )
  }
}

private val DiscardColors = IconButtonColorProvider { colors, isPressed ->
  colors.bareIconButton(isPressed).copy(contentColor = colors.errorText)
}

private const val FAB_INITIAL_SCALE = 0.8f
private val SaveFabPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp)
