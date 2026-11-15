package aktual.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

/**
 * Keys for items that have no id of their own, one per item in [items]. An item keeps its key when
 * others are added or removed around it, or when it's replaced in place by a different value, and
 * equal items still get distinct keys.
 */
@Composable
fun <T> rememberStableKeys(items: ImmutableList<T>): ImmutableList<Int> {
  val keys = remember { StableKeys<T>() }
  return remember(items) { keys.update(items) }
}

internal class StableKeys<T> {
  private var items = emptyList<T>()
  private var keys = emptyList<Int>()
  private var nextKey = 0

  fun update(newItems: List<T>): ImmutableList<Int> {
    val claimed = BooleanArray(items.size)
    val result = arrayOfNulls<Int>(newItems.size)

    // Unchanged at the same index
    for (i in newItems.indices) {
      if (i < items.size && items[i] == newItems[i]) {
        claimed[i] = true
        result[i] = keys[i]
      }
    }

    // Moved, or shifted by an add or remove
    for (i in newItems.indices) {
      if (result[i] != null) continue
      val match = items.indices.firstOrNull { j -> !claimed[j] && items[j] == newItems[i] }
      if (match != null) {
        claimed[match] = true
        result[i] = keys[match]
      }
    }

    // Same count, so a leftover item was edited where it stands
    if (newItems.size == items.size) {
      for (i in newItems.indices) {
        if (result[i] == null && !claimed[i]) {
          claimed[i] = true
          result[i] = keys[i]
        }
      }
    }

    items = newItems
    keys = result.map { it ?: nextKey++ }
    return keys.toImmutableList()
  }
}
