package aktual.core.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListLayoutInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Drags one item of a lazy list by its key. The item follows the finger, and once its middle is
 * over another item [moveOver] gets that item's key, returning true if the list order changed. When
 * it's dropped the item springs into its place, then [settle] runs.
 */
@Stable
class ListDragState(
  private val listState: LazyListState,
  private val scope: CoroutineScope,
  private val moveOver: (key: Any, target: Any) -> Boolean,
  private val settle: suspend () -> Unit,
) {
  var dragged by mutableStateOf<Any?>(null)
    private set

  // The dragged item while it's under a finger, before it settles into its place
  var held by mutableStateOf<Any?>(null)
    private set

  private var draggedDelta by mutableFloatStateOf(0f)
  private var initialOffset = 0
  private var grabY = 0f

  // The layout when the last move was made, so the next one waits until the list shows it
  private var movedLayout: LazyListLayoutInfo? = null

  // How far the dragged item is drawn from its place in the list
  fun offsetOf(key: Any): Float {
    if (dragged != key) return 0f
    val info = itemInfo(key) ?: return 0f
    return initialOffset + draggedDelta - info.offset
  }

  // The item's layout when it was picked up, or null if it can't be
  fun pickUp(key: Any, grabY: Float): LazyListItemInfo? {
    val info = itemInfo(key)
    if (dragged != null || info == null) return null
    initialOffset = info.offset
    draggedDelta = 0f
    this.grabY = grabY
    movedLayout = null
    dragged = key
    held = key
    return info
  }

  fun dragBy(key: Any, delta: Float) {
    if (held != key) return
    draggedDelta += delta
    val layout = listState.layoutInfo
    if (layout !== movedLayout) moveOver(key, layout)
  }

  private fun moveOver(key: Any, layout: LazyListLayoutInfo) {
    val info = itemInfo(key) ?: return
    val middle = (initialOffset + draggedDelta + info.size / 2f).toInt()
    val target =
      layout.visibleItemsInfo.find { other ->
        other.key != key && middle in other.offset until other.offset + other.size
      } ?: return
    if (!moveOver(key, target.key)) return

    // Otherwise the list keeps the moved item in view, scrolling along with it
    val first = listState.firstVisibleItemIndex
    if (info.index == first || target.index == first) {
      listState.requestScrollToItem(first, listState.firstVisibleItemScrollOffset)
    }
    movedLayout = layout
  }

  // Scrolls faster the further past the edge the finger is
  suspend fun autoScroll(key: Any, edge: Float, speed: Float) {
    var lastFrame = withFrameNanos { it }
    while (held == key) {
      val frame = withFrameNanos { it }
      val seconds = (frame - lastFrame) / NANOS_PER_SECOND
      lastFrame = frame
      val factor = edgeFactor(edge)
      if (factor != 0f) {
        listState.scrollBy(factor * speed * seconds)
        dragBy(key, 0f)
      }
    }
  }

  /** Runs [commit] if [key] was held, then springs it into place. */
  fun drop(key: Any, commit: () -> Unit) {
    if (held != key) return
    held = null
    commit()
    scope.launch {
      val info = itemInfo(key)
      if (info != null) {
        animate(
          initialValue = draggedDelta,
          targetValue = (info.offset - initialOffset).toFloat(),
          animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        ) { value, _ ->
          draggedDelta = value
        }
      }
      dragged = null
      settle()
    }
  }

  private fun edgeFactor(edge: Float): Float {
    val info = listState.layoutInfo
    val y = initialOffset + draggedDelta + grabY
    val bottom = info.viewportEndOffset - info.afterContentPadding
    return when {
      y < edge -> -((edge - y) / edge).coerceAtMost(1f)
      y > bottom - edge -> ((y - bottom + edge) / edge).coerceAtMost(1f)
      else -> 0f
    }
  }

  private fun itemInfo(key: Any): LazyListItemInfo? =
    listState.layoutInfo.visibleItemsInfo.find { it.key == key }

  private companion object {
    const val NANOS_PER_SECOND = 1_000_000_000f
  }
}

// Dragging the handle moves its item straight away, with no long press
fun Modifier.dragHandle(
  key: Any,
  haptics: HapticFeedback,
  pickUp: (grabY: Float) -> Boolean,
  dragBy: (delta: Float) -> Unit,
  drop: () -> Unit,
): Modifier =
  pointerInput(key) {
    detectDragGestures(
      onDragStart = { offset -> if (pickUp(offset.y)) haptics.performHapticFeedback(LongPress) },
      onDragEnd = drop,
      onDragCancel = drop,
      onDrag = { change, delta ->
        change.consume()
        dragBy(delta.y)
      },
    )
  }
