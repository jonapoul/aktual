package aktual.core.ui

import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

// Rocks a movable item while editing. Each index starts a little later, so neighbours don't rock
// in step
@Composable
fun Modifier.wiggle(enabled: Boolean, index: Int): Modifier {
  if (!enabled) return this
  val transition = rememberInfiniteTransition()
  val degrees by
    transition.animateFloat(
      initialValue = -WIGGLE_DEGREES,
      targetValue = WIGGLE_DEGREES,
      animationSpec =
        infiniteRepeatable(
          animation = tween(WIGGLE_MILLIS),
          repeatMode = Reverse,
          initialStartOffset = StartOffset(index * WIGGLE_STAGGER_MILLIS),
        ),
    )
  return graphicsLayer { rotationZ = degrees }
}

private const val WIGGLE_DEGREES = 0.5f
private const val WIGGLE_MILLIS = 140
private const val WIGGLE_STAGGER_MILLIS = 60
