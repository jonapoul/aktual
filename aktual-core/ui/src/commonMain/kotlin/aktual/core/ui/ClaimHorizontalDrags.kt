package aktual.core.ui

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.abs

/**
 * Consumes drags that start out mostly horizontal, so parents like the budget side menu don't also
 * treat them as a swipe. For content that follows horizontal drags without consuming them, like a
 * chart marker. Vertical drags still reach scrolling parents.
 */
fun Modifier.claimHorizontalDrags(enabled: Boolean = true): Modifier =
  if (!enabled) {
    this
  } else {
    pointerInput(Unit) {
      awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        val slop = viewConfiguration.touchSlop
        var change = down
        while (change.pressed && (change.position - down.position).getDistance() <= slop) {
          change =
            awaitPointerEvent().changes.firstOrNull { it.id == down.id } ?: return@awaitEachGesture
        }
        val moved = change.position - down.position
        if (!change.pressed || abs(moved.x) <= abs(moved.y)) return@awaitEachGesture
        while (change.pressed) {
          change.consume()
          change =
            awaitPointerEvent().changes.firstOrNull { it.id == down.id } ?: return@awaitEachGesture
        }
      }
    }
  }
