package aktual.budget.model

import kotlin.math.floor

// SORT_INCREMENT in packages/loot-core/src/server/db/sort.ts, the gap between consecutive rows'
// sort_order
const val SORT_INCREMENT = 16384L

// The sort_order that places a new row after [max]. Messages only carry whole numbers, so this
// rounds down first, which still puts it after a fractional order
fun sortOrderAfter(max: Double?): Long = (max?.let(::floor)?.toLong() ?: 0L) + SORT_INCREMENT

data class ShovedSortOrders<T>(val sortOrder: Double, val updates: Map<T, Double>)

/**
 * shoveSortOrders() in packages/loot-core/src/server/db/sort.ts: the sort order that puts an item
 * just before [target] in [items], which are in sort order. When there's no room there, the items
 * from [target] on are pushed up in [ShovedSortOrders.updates]. No target, or one not in [items],
 * puts the item at the end.
 */
fun <T> shoveSortOrders(items: List<Pair<T, Double>>, target: T?): ShovedSortOrders<T> {
  val to = items.indexOfFirst { it.first == target }
  if (target == null || to == -1) {
    val order = items.lastOrNull()?.let { it.second + SORT_INCREMENT } ?: SORT_INCREMENT.toDouble()
    return ShovedSortOrders(order, emptyMap())
  }

  val updates = linkedMapOf<T, Double>()
  val before = items.getOrNull(to - 1)?.second
  if (items[to].second - (before ?: 0.0) <= 2) {
    var order = floor(items[to].second) + SORT_INCREMENT
    for (next in to until items.size) {
      if (order <= items[next].second) break
      updates[items[next].first] = order
      order += SORT_INCREMENT
    }
  }

  val above = items[to].second
  val order = if (before == null) above / 2 else (before + above) / 2
  return ShovedSortOrders(order, updates)
}
