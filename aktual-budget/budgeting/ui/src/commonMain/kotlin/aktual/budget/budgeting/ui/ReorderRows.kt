package aktual.budget.budgeting.ui

import aktual.budget.budgeting.vm.ReorderCategory
import aktual.budget.budgeting.vm.ReorderGroup
import aktual.budget.model.CategoryGroupId
import aktual.budget.model.CategoryId
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.toImmutableList

// One line of the reorder list. A category belongs to the group header above it
@Immutable
internal sealed interface ReorderRow {
  val key: String
  val group: ReorderGroup
}

internal data class HeaderRow(override val group: ReorderGroup, val isCollapsed: Boolean) :
  ReorderRow {
  override val key = "group-${group.id.value}"
}

internal data class ItemRow(val category: ReorderCategory, override val group: ReorderGroup) :
  ReorderRow {
  override val key = "category-${category.id.value}"
}

// A collapsed group still shows the category being dragged out of it
internal fun reorderRows(
  groups: List<ReorderGroup>,
  collapsed: Set<CategoryGroupId>,
  dragged: CategoryId? = null,
): List<ReorderRow> = buildList {
  for (group in groups) {
    val isCollapsed = group.id in collapsed
    add(HeaderRow(group, isCollapsed))
    for (category in group.categories) {
      if (!isCollapsed || category.id == dragged) add(ItemRow(category, group))
    }
  }
}

/**
 * The groups after moving the row with [key] to index [to] of the rows, or null if it can't go
 * there. A category joins the group whose header ends up above it, after any categories hidden by
 * collapsing that group. Group moves need every group collapsed, so the rows are only headers, and
 * income groups stay below the expense ones.
 */
internal fun movedGroups(
  groups: List<ReorderGroup>,
  collapsed: Set<CategoryGroupId>,
  dragged: CategoryId?,
  key: String,
  to: Int,
): List<ReorderGroup>? {
  val rows = reorderRows(groups, collapsed, dragged)
  val from = rows.indexOfFirst { it.key == key }
  if (from < 0 || to !in rows.indices || from == to) return null
  val moving = rows[from]
  val moved = rows.toMutableList().apply { add(to, removeAt(from)) }
  return when (moving) {
    is HeaderRow -> movedHeaders(moving, moved)
    is ItemRow -> movedCategory(moving, moved, collapsed, dragged)
  }
}

private fun movedHeaders(moving: HeaderRow, rows: List<ReorderRow>): List<ReorderGroup>? {
  if (moving.group.isIncome || rows.any { it is ItemRow }) return null
  val order = rows.map { it.group }
  val incomeFirst = order.indexOfFirst { it.isIncome }.takeIf { it >= 0 } ?: order.size
  return if (order.drop(incomeFirst).all { it.isIncome }) order else null
}

private fun movedCategory(
  moving: ItemRow,
  rows: List<ReorderRow>,
  collapsed: Set<CategoryGroupId>,
  dragged: CategoryId?,
): List<ReorderGroup>? {
  if (rows.firstOrNull() !is HeaderRow) return null
  val result = mutableListOf<ReorderGroup>()
  for (row in rows) {
    when (row) {
      is HeaderRow -> {
        val group = row.group
        val unseen =
          if (group.id in collapsed) group.categories.filter { it.id != dragged } else emptyList()
        result += group.copy(categories = unseen.toImmutableList())
      }

      is ItemRow -> {
        val last = result.removeAt(result.lastIndex)
        if (row == moving && last.isIncome != moving.group.isIncome) return null
        result += last.copy(categories = (last.categories + row.category).toImmutableList())
      }
    }
  }
  return result
}
