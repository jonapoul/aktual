package aktual.core.ui

import aktual.core.theme.LocalTheme
import aktual.core.theme.Theme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition.Companion.Above
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults.rememberTooltipPositionProvider
import androidx.compose.material3.TooltipState
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.window.PopupPositionProvider
import kotlinx.coroutines.launch

/** Basically a wrapper around the blurring functionality */
@Composable
fun AktualTooltip(
  imageVector: ImageVector,
  contentDescription: String?,
  tooltipText: String,
  modifier: Modifier = Modifier,
  state: TooltipState = rememberTooltipState(isPersistent = true),
  positionProvider: PopupPositionProvider = rememberTooltipPositionProvider(Above),
  theme: Theme = LocalTheme.current,
) {
  val scope = rememberCoroutineScope()

  TooltipBox(
    modifier = modifier,
    positionProvider = positionProvider,
    state = state,
    tooltip = {
      val dialogBlurState = LocalDialogBlurState.current
      DisposableEffect(Unit) {
        dialogBlurState.activeDialogCount++
        onDispose { dialogBlurState.activeDialogCount-- }
      }

      PlainTooltip(
        modifier = Modifier.clip(CardShape),
        caretShape = CardShape,
        shape = CardShape,
        contentColor = theme.tooltipText,
        containerColor = theme.tooltipBackground,
        content = { Text(tooltipText) },
      )
    },
    content = {
      NormalIconButton(
        imageVector = imageVector,
        contentDescription = contentDescription,
        onClick = { if (state.isVisible) state.dismiss() else scope.launch { state.show() } },
      )
    },
  )
}
