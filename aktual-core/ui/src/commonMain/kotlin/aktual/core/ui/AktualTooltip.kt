package aktual.core.ui

import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition.Companion.Above
import androidx.compose.material3.TooltipDefaults.rememberTooltipPositionProvider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties

// Not TooltipBox: its popup isn't focusable on desktop, so taps outside it reach the content
// underneath instead of only dismissing the tooltip
@Composable
fun AktualTooltip(
  imageVector: ImageVector,
  contentDescription: String?,
  tooltipText: String,
  modifier: Modifier = Modifier,
  kind: ButtonKind = Bare,
  positionProvider: PopupPositionProvider = rememberTooltipPositionProvider(Above),
) {
  var isVisible by remember { mutableStateOf(false) }

  Box(modifier = modifier) {
    AktualIconButton(
      kind = kind,
      imageVector = imageVector,
      contentDescription = contentDescription,
      onClick = { isVisible = !isVisible },
    )

    if (isVisible) {
      Popup(
        popupPositionProvider = positionProvider,
        onDismissRequest = { isVisible = false },
        properties = PopupProperties(focusable = true),
      ) {
        val dialogBlurState = LocalDialogBlurState.current
        DisposableEffect(Unit) {
          dialogBlurState.activeDialogCount++
          onDispose { dialogBlurState.activeDialogCount-- }
        }

        Box(
          modifier =
            Modifier.sizeIn(minWidth = 40.dp, minHeight = 24.dp, maxWidth = 200.dp)
              .clip(CardShape)
              .background(colors.tooltipBackground)
              .padding(horizontal = 8.dp, vertical = 4.dp),
          contentAlignment = CenterStart,
        ) {
          Text(text = tooltipText, color = colors.tooltipText, style = typography.bodySmall)
        }
      }
    }
  }
}
