package aktual.budget.budgeting.ui

import aktual.budget.budgeting.vm.ReorderGroup
import aktual.budget.model.CategoryGroupId
import aktual.budget.model.CategoryId
import aktual.core.ui.ListDragState
import aktual.core.ui.dragHandle
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedback
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toPersistentSet
import kotlinx.coroutines.CoroutineScope

// Holds the order while a row is dragged, and hands it to commit when it's dropped. Every group
// collapses while a group is dragged, so groups only pass other headers
@Stable
internal class ReorderListState(
  private val listState: LazyListState,
  scope: CoroutineScope,
  private val commit: (row: ReorderRow, groups: List<ReorderGroup>) -> Unit,
) {
  var collapsed by mutableStateOf<PersistentSet<CategoryGroupId>>(persistentSetOf())
    private set

  // The groups from the view model, kept up to date by the screen
  var groups: List<ReorderGroup> = emptyList()

  private var draft by mutableStateOf<List<ReorderGroup>?>(null)
  private var draggedRow by mutableStateOf<ReorderRow?>(null)

  val isDraggingGroup: Boolean
    get() = draggedRow is HeaderRow

  private val draggedCategory: CategoryId?
    get() = (draggedRow as? ItemRow)?.category?.id

  private val drag =
    ListDragState(
      listState = listState,
      scope = scope,
      moveOver = { key, target -> moveOver(key as String, target) },
      settle = {
        draggedRow = null
        draft = null
      },
    )

  // The dragged row while it's under a finger, before it settles into its place
  val held: String?
    get() = drag.held as? String

  val dragged: String?
    get() = draggedRow?.key

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

  fun offsetOf(key: String): Float = drag.offsetOf(key)

  fun pickUp(row: ReorderRow, grabY: Float): Boolean {
    val info = drag.pickUp(row.key, grabY) ?: return false
    draft = groups
    draggedRow = row

    // Collapsing the groups moves the header, so keep it near the finger
    if (row is HeaderRow) {
      val index = groups.indexOfFirst { it.id == row.group.id }
      val above = if (info.size > 0) info.offset / info.size else 0
      listState.requestScrollToItem((index - above).coerceAtLeast(0))
    }
    return true
  }

  fun dragBy(key: String, delta: Float) = drag.dragBy(key, delta)

  private fun moveOver(key: String, target: Any): Boolean {
    val groups = draft ?: return false
    val collapsed = shownCollapsed(groups)
    val to = reorderRows(groups, collapsed, draggedCategory).indexOfFirst { it.key == target }
    val moved = movedGroups(groups, collapsed, draggedCategory, key, to) ?: return false
    draft = moved
    return true
  }

  suspend fun autoScroll(key: String, edge: Float, speed: Float) = drag.autoScroll(key, edge, speed)

  fun drop(key: String) =
    drag.drop(key) {
      val row = draggedRow
      val moved = draft
      if (row != null && moved != null && moved != groups) commit(row, moved)
    }

  private fun shownCollapsed(groups: List<ReorderGroup>): Set<CategoryGroupId> =
    if (isDraggingGroup) allIds(groups) else collapsed

  private fun allIds(groups: List<ReorderGroup>) = groups.mapTo(mutableSetOf()) { it.id }
}

internal fun Modifier.reorderHandle(
  state: ReorderListState,
  row: ReorderRow,
  haptics: HapticFeedback,
): Modifier =
  dragHandle(
    key = row.key,
    haptics = haptics,
    pickUp = { grabY -> state.pickUp(row, grabY) },
    dragBy = { delta -> state.dragBy(row.key, delta) },
    drop = { state.drop(row.key) },
  )
