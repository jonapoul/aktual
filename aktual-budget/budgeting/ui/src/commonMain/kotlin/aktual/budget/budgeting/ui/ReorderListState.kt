package aktual.budget.budgeting.ui

import aktual.budget.budgeting.vm.ReorderGroup
import aktual.budget.model.CategoryGroupId
import aktual.budget.model.CategoryId
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
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toPersistentSet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

// Holds the order while a row is dragged, and hands it to commit when it's dropped. The dragged row
// follows the finger, and passes another once its middle is over that one. Every group collapses
// while a group is dragged, so groups only pass other headers
@Stable
internal class ReorderListState(
  private val listState: LazyListState,
  private val scope: CoroutineScope,
  private val commit: (row: ReorderRow, groups: List<ReorderGroup>) -> Unit,
) {
  var collapsed by mutableStateOf<PersistentSet<CategoryGroupId>>(persistentSetOf())
    private set

  // The groups from the view model, kept up to date by the screen
  var groups: List<ReorderGroup> = emptyList()

  private var draft by mutableStateOf<List<ReorderGroup>?>(null)
  private var draggedRow by mutableStateOf<ReorderRow?>(null)

  // The dragged row while it's under a finger, before it settles into its place
  var held by mutableStateOf<String?>(null)
    private set

  private var draggedDelta by mutableFloatStateOf(0f)
  private var initialOffset = 0
  private var grabY = 0f

  // The layout when the last move was made, so the next one waits until the list shows it
  private var movedLayout: LazyListLayoutInfo? = null

  val dragged: String?
    get() = draggedRow?.key

  val isDraggingGroup: Boolean
    get() = draggedRow is HeaderRow

  private val draggedCategory: CategoryId?
    get() = (draggedRow as? ItemRow)?.category?.id

  fun rows(groups: List<ReorderGroup>): List<ReorderRow> =
    reorderRows(draft ?: groups, shownCollapsed(groups), draggedCategory)

  fun toggle(id: CategoryGroupId) {
    collapsed = if (id in collapsed) collapsed.removing(id) else collapsed.adding(id)
  }

  fun setAllCollapsed(collapse: Boolean) {
    collapsed = if (collapse) groups.map { it.id }.toPersistentSet() else persistentSetOf()
  }

  // Moves a row one place without dragging, for accessibility actions
  fun moveBy(row: ReorderRow, delta: Int) {
    if (draggedRow != null) return
    val collapsed = if (row is HeaderRow) allIds(groups) else collapsed
    val rows = reorderRows(groups, collapsed)
    val from = rows.indexOfFirst { it.key == row.key }
    val moved = movedGroups(groups, collapsed, null, row.key, from + delta) ?: return
    commit(row, moved)
  }

  fun canMoveBy(row: ReorderRow, delta: Int): Boolean {
    val collapsed = if (row is HeaderRow) allIds(groups) else collapsed
    val from = reorderRows(groups, collapsed).indexOfFirst { it.key == row.key }
    return movedGroups(groups, collapsed, null, row.key, from + delta) != null
  }

  // How far the dragged row is drawn from its place in the list
  fun offsetOf(key: String): Float {
    if (dragged != key) return 0f
    val info = itemInfo(key) ?: return 0f
    return initialOffset + draggedDelta - info.offset
  }

  fun pickUp(row: ReorderRow, grabY: Float): Boolean {
    val info = itemInfo(row.key)
    if (draggedRow != null || info == null) return false
    initialOffset = info.offset
    draggedDelta = 0f
    this.grabY = grabY
    movedLayout = null
    draft = groups
    draggedRow = row
    held = row.key

    // Collapsing the groups moves the header, so keep it near the finger
    if (row is HeaderRow) {
      val index = groups.indexOfFirst { it.id == row.group.id }
      val above = if (info.size > 0) initialOffset / info.size else 0
      listState.requestScrollToItem((index - above).coerceAtLeast(0))
    }
    return true
  }

  fun dragBy(key: String, delta: Float) {
    if (held != key) return
    draggedDelta += delta
    val layout = listState.layoutInfo
    if (layout !== movedLayout) moveOver(key, layout)
  }

  private fun moveOver(key: String, layout: LazyListLayoutInfo) {
    val groups = draft ?: return
    val info = itemInfo(key) ?: return
    val middle = (initialOffset + draggedDelta + info.size / 2f).toInt()
    val target =
      layout.visibleItemsInfo.find { other ->
        other.key != key && middle in other.offset until other.offset + other.size
      } ?: return
    val collapsed = shownCollapsed(groups)
    val to = reorderRows(groups, collapsed, draggedCategory).indexOfFirst { it.key == target.key }
    val moved = movedGroups(groups, collapsed, draggedCategory, key, to) ?: return

    // Otherwise the list keeps the moved row in view, scrolling along with it
    val first = listState.firstVisibleItemIndex
    if (info.index == first || target.index == first) {
      listState.requestScrollToItem(first, listState.firstVisibleItemScrollOffset)
    }
    movedLayout = layout
    draft = moved
  }

  // Scrolls faster the further past the edge the finger is
  suspend fun autoScroll(key: String, edge: Float, speed: Float) {
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

  fun drop(key: String) {
    if (held != key) return
    held = null
    val row = draggedRow
    val moved = draft
    if (row != null && moved != null && moved != groups) commit(row, moved)
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
      draggedRow = null
      draft = null
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

  private fun shownCollapsed(groups: List<ReorderGroup>): Set<CategoryGroupId> =
    if (isDraggingGroup) allIds(groups) else collapsed

  private fun allIds(groups: List<ReorderGroup>) = groups.mapTo(mutableSetOf()) { it.id }

  private fun itemInfo(key: String): LazyListItemInfo? =
    listState.layoutInfo.visibleItemsInfo.find { it.key == key }

  private companion object {
    const val NANOS_PER_SECOND = 1_000_000_000f
  }
}

// Dragging the handle moves its row straight away, with no long press
internal fun Modifier.reorderHandle(
  state: ReorderListState,
  row: ReorderRow,
  haptics: HapticFeedback,
): Modifier =
  pointerInput(state, row.key) {
    detectDragGestures(
      onDragStart = { offset ->
        if (state.pickUp(row, offset.y)) haptics.performHapticFeedback(LongPress)
      },
      onDragEnd = { state.drop(row.key) },
      onDragCancel = { state.drop(row.key) },
      onDrag = { change, delta ->
        change.consume()
        state.dragBy(row.key, delta.y)
      },
    )
  }
