package aktual.core.ui

import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition.Companion.Above
import androidx.compose.material3.TooltipDefaults.rememberTooltipPositionProvider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.IntOffset
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
  val visibleState = remember { MutableTransitionState(false) }
  val isShowing = visibleState.targetState

  val dialogBlurState = LocalDialogBlurState.current
  DisposableEffect(isShowing) {
    if (isShowing) dialogBlurState.activeDialogCount++
    onDispose { if (isShowing) dialogBlurState.activeDialogCount-- }
  }

  Box(modifier = modifier) {
    AktualIconButton(
      kind = kind,
      imageVector = imageVector,
      contentDescription = contentDescription,
      onClick = { visibleState.targetState = !isShowing },
    )

    // Keep the popup until the exit animation finishes
    if (isShowing || visibleState.currentState) {
      Popup(
        popupPositionProvider = positionProvider,
        onDismissRequest = { visibleState.targetState = false },
        properties = PopupProperties(focusable = true),
      ) {
        AnimatedVisibility(visibleState = visibleState, enter = Enter, exit = Exit) {
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
}

private val Origin = TransformOrigin(pivotFractionX = 0.5f, pivotFractionY = 1f)

private val Enter =
  fadeIn(tween(durationMillis = 120)) +
    scaleIn(
      animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium),
      initialScale = 0.6f,
      transformOrigin = Origin,
    ) +
    slideInVertically(
      animationSpec =
        spring(
          dampingRatio = Spring.DampingRatioLowBouncy,
          stiffness = Spring.StiffnessMedium,
          visibilityThreshold = IntOffset.VisibilityThreshold,
        ),
      initialOffsetY = { it / 3 },
    )

private val Exit =
  fadeOut(tween(durationMillis = 100)) +
    scaleOut(
      animationSpec = tween(durationMillis = 100),
      targetScale = 0.85f,
      transformOrigin = Origin,
    ) +
    slideOutVertically(animationSpec = tween(durationMillis = 100), targetOffsetY = { it / 6 })
