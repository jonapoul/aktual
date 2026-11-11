package aktual.budget.accounts.ui.manage

import aktual.budget.accounts.vm.manage.ManagedGroup
import aktual.budget.model.AccountGroupId
import aktual.budget.model.AccountId
import aktual.core.ui.ListDragState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Holds the order while a line is dragged, and saves it on drop with [moveAccount] or [moveGroup].
 * The order stays shown until the saved one comes back from the view model, so rows don't jump back
 * in between.
 */
@Stable
internal class ManageDragState(
  private val listState: LazyListState,
  scope: CoroutineScope,
  private val moveAccount: (id: AccountId, group: AccountGroupId?, before: AccountId?) -> Unit,
  private val moveGroup: (id: AccountGroupId, before: AccountGroupId?) -> Unit,
) {
  // The sections from the view model, kept up to date by the screen
  var sections by mutableStateOf<ManageSections?>(null)

  private var draft by mutableStateOf<ManageSections?>(null)
  private var draggedLine by mutableStateOf<ManageLine?>(null)
  private var committed: ManageSections? = null

  private val drag =
    ListDragState(
      listState = listState,
      scope = scope,
      moveOver = { _, target -> moveOver(target) },
      settle = ::settle,
    )

  // The dragged line while it's under a finger, before it settles into its place
  val held: String?
    get() = drag.held as? String

  val dragged: String?
    get() = draggedLine?.key

  fun lines(side: Side, sections: ManageSections): List<ManageLine> =
    manageLines(side, (draft ?: sections)[side], collapsed = draggedLine is GroupLine)

  fun canDrag(line: ManageLine): Boolean =
    line is AccountLine || line is GroupLine && line.group.id != null

  // Moves a line one place without dragging, for accessibility actions
  fun moveBy(line: ManageLine, delta: Int) {
    if (draft != null) return
    val sections = sections ?: return
    val moved = movedBy(sections, line, delta) ?: return
    commit(line, sections, moved)
  }

  fun canMoveBy(line: ManageLine, delta: Int): Boolean =
    sections?.let { movedBy(it, line, delta) } != null

  fun offsetOf(key: String): Float = drag.offsetOf(key)

  fun pickUp(line: ManageLine, grabY: Float): Boolean {
    val sections = sections
    if (sections == null || draft != null || !canDrag(line)) return false
    val info = drag.pickUp(line.key, grabY) ?: return false
    draft = sections
    draggedLine = line

    // Collapsing the groups moves the header, so keep it near the finger
    if (line is GroupLine) {
      val index = collapsedIndex(sections, line)
      val above = if (info.size > 0) info.offset / info.size else 0
      listState.requestScrollToItem((index - above).coerceAtLeast(0))
    }
    return true
  }

  fun dragBy(key: String, delta: Float) = drag.dragBy(key, delta)

  suspend fun autoScroll(key: String, edge: Float, speed: Float) = drag.autoScroll(key, edge, speed)

  fun drop(key: String) =
    drag.drop(key) {
      val line = draggedLine ?: return@drop
      val before = sections ?: return@drop
      val after = draft ?: return@drop
      if (commit(line, before, after)) committed = before
    }

  private fun moveOver(target: Any): Boolean {
    val draft = draft ?: return false
    val line = draggedLine ?: return false
    val to = linesOf(draft, line).indexOfFirst { it.key == target }
    this.draft = moved(draft, line, to) ?: return false
    return true
  }

  private suspend fun settle() {
    draggedLine = null
    val before = committed
    if (before != null) {
      withTimeoutOrNull(SETTLE_TIMEOUT) { snapshotFlow { sections }.first { it != before } }
    }
    committed = null
    draft = null
  }

  private fun movedBy(sections: ManageSections, line: ManageLine, delta: Int): ManageSections? {
    if (!canDrag(line)) return null
    val from = linesOf(sections, line).indexOfFirst { it.key == line.key }
    return moved(sections, line, from + delta)
  }

  private fun linesOf(sections: ManageSections, line: ManageLine) =
    manageLines(line.side, sections[line.side], collapsed = line is GroupLine)

  private fun moved(sections: ManageSections, line: ManageLine, to: Int): ManageSections? {
    val groups = sections[line.side]
    return when (line) {
      is GroupLine ->
        movedGroup(line.side, groups, line.key, to)?.let { order ->
          ManageSections(sections.onBudget.inOrderOf(order), sections.offBudget.inOrderOf(order))
        }

      is AccountLine ->
        movedAccount(line.side, groups, line.key, to)?.let { sections.with(line.side, it) }

      is EmptyLine -> null
    }
  }

  // Sends the move that turns [before] into [after], returning false if nothing moved
  private fun commit(line: ManageLine, before: ManageSections, after: ManageSections): Boolean {
    val groups = after[line.side]
    when (line) {
      is AccountLine -> {
        if (groups.layout() == before[line.side].layout()) return false
        val id = line.account.id
        val group = groups.first { g -> g.accounts.any { it.id == id } }
        val next = group.accounts.getOrNull(group.accounts.indexOfFirst { it.id == id } + 1)
        moveAccount(id, group.id, next?.id)
      }

      is GroupLine -> {
        val id = line.group.id ?: return false
        val order = groups.mapNotNull { it.id }
        if (order == before[line.side].mapNotNull { it.id }) return false
        moveGroup(id, order.getOrNull(order.indexOf(id) + 1))
      }

      is EmptyLine -> return false
    }
    return true
  }

  // Each side's header comes before its groups, which are only headers while a group drags
  private fun collapsedIndex(sections: ManageSections, line: GroupLine): Int {
    val index = sections[line.side].indexOfFirst { it.id == line.group.id }
    return when (line.side) {
      OnBudget -> 1 + index
      OffBudget -> 1 + sections.onBudget.size + 1 + index
    }
  }

  private fun List<ManagedGroup>.layout() = map { g -> g.id to g.accounts.map { it.id } }

  private companion object {
    val SETTLE_TIMEOUT = 2.seconds
  }
}
