package aktual.core.ui

import aktual.core.icons.material.Delete
import aktual.core.icons.material.Edit
import aktual.core.icons.material.MaterialIcons
import aktual.core.theme.Colors
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import kotlin.math.roundToInt
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * Lets [content] be swiped left to reveal [actions] behind it. Only one item in a list should be
 * open at a time, so the caller holds [isOpen] and updates it from [onOpenChange]. [content] needs
 * an opaque background.
 */
@Composable
fun SwipeToReveal(
  actions: ImmutableList<SwipeAction>,
  isOpen: Boolean,
  onOpenChange: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
  shape: Shape = CardShape,
  content: @Composable BoxScope.() -> Unit,
) {
  val openOffsetPx = with(LocalDensity.current) { (SwipeButtonWidth * actions.size).toPx() }
  val swipeState =
    remember(openOffsetPx) {
      AnchoredDraggableState(initialValue = SwipeState.Closed).apply {
        updateAnchors(
          DraggableAnchors {
            SwipeState.Closed at 0f
            SwipeState.Open at -openOffsetPx
          },
        )
      }
    }

  // Tell the parent when this item settles, so it can keep only one item open
  val currentOnOpenChange by rememberUpdatedState(onOpenChange)
  LaunchedEffect(swipeState) {
    snapshotFlow { swipeState.settledValue }
      .collect { settled -> currentOnOpenChange(settled == Open) }
  }

  // Claim the open slot as soon as a drag starts, so any other open item closes straight away
  val interactionSource = remember { MutableInteractionSource() }
  LaunchedEffect(interactionSource) {
    interactionSource.interactions.collect { interaction ->
      if (interaction is DragInteraction.Start) currentOnOpenChange(true)
    }
  }

  LaunchedEffect(isOpen) {
    if (!isOpen && swipeState.currentValue != Closed) {
      swipeState.animateTo(Closed)
    }
  }

  val revealedPx = { swipeState.offset.let { x -> if (x.isNaN()) 0f else -x } }

  Box(modifier = modifier.fillMaxWidth().clip(shape)) {
    Row(modifier = Modifier.matchParentSize(), horizontalArrangement = Arrangement.End) {
      actions.fastForEachIndexed { index, action ->
        SwipeButton(
          action = action,
          revealedPx = revealedPx,
          indexFromEnd = actions.lastIndex - index,
          // The content's trailing corners are rounded, so bleed the first button under them
          bleed = if (index == 0) SwipeButtonBleed else 0.dp,
        )
      }
    }

    Box(
      modifier =
        Modifier.offset {
            val x = swipeState.offset
            IntOffset(x = if (x.isNaN()) 0 else x.roundToInt(), y = 0)
          }
          .anchoredDraggable(
            state = swipeState,
            orientation = Horizontal,
            interactionSource = interactionSource,
          ),
      content = content,
    )
  }
}

@Immutable
data class SwipeAction(
  val text: String,
  val icon: ImageVector,
  val background: Color,
  val foreground: Color,
  val onClick: () -> Unit,
)

// Resting positions of the content: closed, or swiped left to reveal the actions
private enum class SwipeState {
  Closed,
  Open,
}

@Composable
private fun SwipeButton(
  action: SwipeAction,
  revealedPx: () -> Float,
  indexFromEnd: Int,
  bleed: Dp,
) {
  val widthPx = with(LocalDensity.current) { SwipeButtonWidth.toPx() }

  // 0 while hidden behind the content, 1 once fully uncovered. Buttons nearer the end uncover first
  fun progress() = ((revealedPx() - indexFromEnd * widthPx) / widthPx).coerceIn(0f, 1f)

  Column(
    modifier =
      Modifier.fillMaxHeight()
        .width(SwipeButtonWidth)
        .drawBehind {
          val bleedPx = bleed.toPx()
          drawRect(
            color = action.background,
            topLeft = Offset(x = -bleedPx, y = 0f),
            size = Size(width = size.width + bleedPx, height = size.height),
          )
        }
        .clickable(onClick = action.onClick),
    horizontalAlignment = CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    // Pops in with a slight overshoot and a twist as the content slides away
    Icon(
      modifier =
        Modifier.graphicsLayer {
          val progress = progress()
          val scale = EaseOutBack.transform(progress)
          alpha = progress
          scaleX = scale
          scaleY = scale
          rotationZ = (1f - progress) * -ICON_TWIST_DEGREES
        },
      imageVector = action.icon,
      contentDescription = null,
      tint = action.foreground,
    )

    Text(
      modifier =
        Modifier.graphicsLayer {
          val progress = progress()
          alpha = progress
          translationY = (1f - progress) * LabelRise.toPx()
        },
      text = action.text,
      color = action.foreground,
    )
  }
}

private const val ICON_TWIST_DEGREES = 90f
private val LabelRise = 8.dp
private val SwipeButtonWidth = 80.dp
private val SwipeButtonBleed = 24.dp

@Preview
@Composable
private fun PreviewSwipeToReveal(@PreviewParameter(ColoredParameters::class) colors: Colors) =
  PreviewWithColors(colors) {
    SwipeToReveal(
      actions =
        persistentListOf(
          SwipeAction("Rename", MaterialIcons.Edit, colors.tableBackground, colors.tableText) {},
          SwipeAction(
            "Delete",
            MaterialIcons.Delete,
            colors.errorText,
            colors.errorText.contrastingTextColor(),
          ) {},
        ),
      isOpen = false,
      onOpenChange = {},
    ) {
      Box(Modifier.fillMaxWidth().height(64.dp).background(colors.tableBackground))
    }
  }
