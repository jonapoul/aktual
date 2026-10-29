package aktual.budget.reports.ui.dashboard

import aktual.budget.model.WidgetId
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

// Holds the order while editing, which is only written to the database once editing finishes. The
// dragged card follows the finger, and passes another once it's over that one's middle. Offsets are
// in the list's item space, where 0 is the top of the content below the top bar
@Stable
internal class DashboardReorderState(
  private val listState: LazyListState,
  private val scope: CoroutineScope,
) {
  var isEditing by mutableStateOf(false)
    private set

  var order by mutableStateOf(persistentListOf<WidgetId>())
    private set

  private var saved by mutableStateOf(persistentListOf<WidgetId>())
  private var synced: List<WidgetId>? = null

  val hasChanges: Boolean
    get() = order != saved

  var dragged by mutableStateOf<WidgetId?>(null)
    private set

  // The dragged card while it's under a finger, before it settles into its place
  var held by mutableStateOf<WidgetId?>(null)
    private set

  private var draggedDelta by mutableFloatStateOf(0f)
  private var initialOffset = 0
  private var grabY = 0f

  // How far the dragged card is drawn from its place in the list
  fun offsetOf(id: WidgetId): Float {
    if (dragged != id) return 0f
    val info = itemInfo(id) ?: return 0f
    return initialOffset + draggedDelta - info.offset
  }

  // While editing, keeps the edited order but takes in any reports added or removed meanwhile
  fun sync(ids: List<WidgetId>) {
    if (ids == synced) return
    synced = ids
    saved = ids.toPersistentList()
    order =
      if (isEditing) {
        (order.filter { it in saved } + saved.filter { it !in order }).toPersistentList()
      } else {
        saved
      }
  }

  fun startEditing() {
    isEditing = true
  }

  // Returns the new order if it changed. It stays shown until the database catches up
  fun finishEditing(): ImmutableList<WidgetId>? {
    dropHeld()
    isEditing = false
    val changed = order.takeIf { hasChanges }
    saved = order
    return changed
  }

  fun discard() {
    dropHeld()
    isEditing = false
    order = saved
  }

  fun pickUp(id: WidgetId, grabY: Float): Boolean {
    val info = itemInfo(id)
    if (dragged != null || info == null) return false
    initialOffset = info.offset
    draggedDelta = 0f
    isEditing = true
    this.grabY = grabY
    dragged = id
    held = id
    return true
  }

  fun dragBy(id: WidgetId, delta: Float) {
    if (held != id) return
    draggedDelta += delta
    val info = itemInfo(id) ?: return
    val middle = (initialOffset + draggedDelta + info.size / 2f).toInt()
    val target =
      listState.layoutInfo.visibleItemsInfo.find { other ->
        other.key != id.value &&
          middle in other.offset until other.offset + other.size &&
          order.any { it.value == other.key }
      } ?: return
    val from = order.indexOf(id)
    val to = order.indexOfFirst { it.value == target.key }

    // Otherwise the list keeps the moved card in view, scrolling along with it
    val first = listState.firstVisibleItemIndex
    if (from == first || to == first) {
      val offset = listState.firstVisibleItemScrollOffset
      listState.requestScrollToItem(first, offset)
    }
    order = order.removingAt(from).addingAt(to, id)
  }

  // Scrolls faster the further past the edge the finger is. The card stays under the finger, and
  // passes the cards scrolling under it
  suspend fun autoScroll(id: WidgetId, edge: Float, speed: Float) {
    var lastFrame = withFrameNanos { it }
    while (held == id) {
      val frame = withFrameNanos { it }
      val seconds = (frame - lastFrame) / NANOS_PER_SECOND
      lastFrame = frame
      val factor = edgeFactor(edge)
      if (factor != 0f) {
        listState.scrollBy(factor * speed * seconds)
        dragBy(id, 0f)
      }
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

  fun dropHeld() {
    held?.let(::drop)
  }

  fun drop(id: WidgetId) {
    if (held != id) return
    held = null
    scope.launch {
      val info = itemInfo(id)
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
    }
  }

  private fun itemInfo(id: WidgetId): LazyListItemInfo? =
    listState.layoutInfo.visibleItemsInfo.find { it.key == id.value }

  companion object {
    // Keeps editing and the unsaved order across config changes
    fun saver(listState: LazyListState, scope: CoroutineScope) =
      listSaver(
        save = { state -> if (state.isEditing) state.order.map { it.value } else emptyList() },
        restore = { saved ->
          DashboardReorderState(listState, scope).apply {
            isEditing = saved.isNotEmpty()
            order = saved.map(::WidgetId).toPersistentList()
          }
        },
      )
  }
}

// A long press on the card picks it up to drag it, starting editing if it hasn't already
internal fun Modifier.reorderHandle(
  state: DashboardReorderState,
  id: WidgetId,
  haptics: HapticFeedback,
): Modifier =
  pointerInput(state, id) {
    detectDragGesturesAfterLongPress(
      onDragStart = { offset ->
        if (state.pickUp(id, offset.y)) haptics.performHapticFeedback(LongPress)
      },
      onDragEnd = { state.drop(id) },
      onDragCancel = { state.drop(id) },
      onDrag = { change, delta ->
        change.consume()
        state.dragBy(id, delta.y)
      },
    )
  }

private const val NANOS_PER_SECOND = 1_000_000_000f
