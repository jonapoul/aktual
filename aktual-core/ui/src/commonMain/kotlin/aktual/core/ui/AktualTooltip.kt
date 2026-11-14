package aktual.core.ui

import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipDefaults.rememberTooltipPositionProvider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
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
  positionProvider: PopupPositionProvider = rememberTooltipPositionProvider(positioning = Above),
) {
  val visibleState = remember { MutableTransitionState(false) }
  val isShowing = visibleState.targetState
  val anchorTracker = remember(positionProvider) { AnchorTrackingProvider(positionProvider) }

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
        popupPositionProvider = anchorTracker,
        onDismissRequest = { visibleState.targetState = false },
        properties = PopupProperties(focusable = true),
      ) {
        AnimatedVisibility(
          visibleState = visibleState,
          enter = fadeIn(tween(durationMillis = 120)),
          exit = fadeOut(tween(durationMillis = 100)),
        ) {
          val scale by
            transition.animateFloat(transitionSpec = { animationSpec(targetState) }) { state ->
              when (state) {
                PreEnter -> 0.6f
                Visible -> 1f
                PostExit -> 0.85f
              }
            }
          val shift by
            transition.animateFloat(transitionSpec = { animationSpec(targetState) }) { state ->
              when (state) {
                PreEnter -> 1f / 3
                Visible -> 0f
                PostExit -> 1f / 6
              }
            }

          Box(
            modifier =
              Modifier.graphicsLayer {
                  // Grow out of the button and shrink back into it, wherever it sits
                  val origin = anchorTracker.origin
                  transformOrigin = origin
                  scaleX = scale
                  scaleY = scale
                  translationX = (origin.pivotFractionX - CENTRE) * size.width * shift
                  translationY =
                    (if (origin.pivotFractionY > CENTRE) 1 else -1) * size.height * shift
                }
                .sizeIn(minWidth = 40.dp, minHeight = 24.dp, maxWidth = 200.dp)
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

private const val CENTRE = 0.5f

private fun animationSpec(target: EnterExitState) =
  if (target == Visible) {
    spring<Float>(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium)
  } else {
    tween(durationMillis = 100)
  }

/** Wraps [delegate] to record where the anchor sits relative to the popup it places */
private class AnchorTrackingProvider(private val delegate: PopupPositionProvider) :
  PopupPositionProvider {
  var origin by mutableStateOf(TransformOrigin(pivotFractionX = 0.5f, pivotFractionY = 1f))
    private set

  override fun calculatePosition(
    anchorBounds: IntRect,
    windowSize: IntSize,
    layoutDirection: LayoutDirection,
    popupContentSize: IntSize,
  ): IntOffset {
    val position =
      delegate.calculatePosition(anchorBounds, windowSize, layoutDirection, popupContentSize)
    if (popupContentSize.width > 0 && popupContentSize.height > 0) {
      val anchor = anchorBounds.center
      val pivotX = (anchor.x - position.x).toFloat() / popupContentSize.width
      val isAbove = position.y + popupContentSize.height / 2 < anchor.y
      origin =
        TransformOrigin(
          pivotFractionX = pivotX.coerceIn(0f, 1f),
          pivotFractionY = if (isAbove) 1f else 0f,
        )
    }
    return position
  }
}
