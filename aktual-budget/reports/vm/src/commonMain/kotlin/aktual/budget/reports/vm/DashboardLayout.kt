package aktual.budget.reports.vm

import aktual.budget.db.dao.DashboardDao.Companion.MAX_WIDTH
import aktual.budget.model.WidgetId

// The list shows widgets in the browser's grid reading order, so reordering the list moves widgets
// into each other's grid slots, each keeping its own size. That leaves the browser's layout as it
// was when the sizes allow. Otherwise the widgets are packed onto the grid in the new order.
// Widgets missing from the order keep their place after the ordered ones
internal fun reorderLayout(boxes: List<WidgetBox>, order: List<WidgetId>): List<WidgetBox> {
  val slots = boxes.sortedWith(compareBy({ it.y }, { it.x }))
  val byId = boxes.associateBy { it.id }
  val ordered = order.distinct().mapNotNull(byId::get)
  val orderedIds = ordered.map { it.id }.toSet()
  val all = ordered + slots.filter { it.id !in orderedIds }
  val swapped = all.zip(slots) { box, slot -> box.copy(x = slot.x, y = slot.y) }
  return if (swapped.fits()) swapped else pack(all)
}

private fun List<WidgetBox>.fits(): Boolean =
  all { it.x >= 0 && it.x + it.span <= MAX_WIDTH } &&
    withIndex().none { (i, a) -> drop(i + 1).any { b -> a overlaps b } }

// Each widget goes in the first free place after the one before it, reading left to right and
// then down
private fun pack(ordered: List<WidgetBox>): List<WidgetBox> {
  val placed = mutableListOf<WidgetBox>()
  var x = 0L
  var y = 0L
  for (box in ordered) {
    while (x + box.span > MAX_WIDTH || placed.any { it overlaps box.copy(x = x, y = y) }) {
      if (x + box.span > MAX_WIDTH) {
        x = 0L
        y++
      } else {
        x++
      }
    }
    placed += box.copy(x = x, y = y)
  }
  return placed
}

private infix fun WidgetBox.overlaps(other: WidgetBox): Boolean =
  x < other.x + other.span && other.x < x + span && y < other.y + other.depth && other.y < y + depth

private val WidgetBox.span: Long
  get() = width.coerceIn(1L, MAX_WIDTH)

private val WidgetBox.depth: Long
  get() = height.coerceAtLeast(1L)
