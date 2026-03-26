package aktual.app.nav

import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation3.runtime.NavKey
import logcat.logcat

fun SnapshotStateList<NavKey>.debugPush(route: NavKey) {
  logcat.v(TAG) { "Push $route - backStack=[$backStackString]" }
  add(route)
}

fun SnapshotStateList<NavKey>.debugPop(): Boolean {
  logcat.v(TAG) { "Pop - backStack=[$backStackString]" }
  if (size <= 1) return false
  removeAt(lastIndex)
  return true
}

fun SnapshotStateList<NavKey>.debugPopUpToAndPush(
  route: NavKey,
  predicate: (NavKey) -> Boolean,
  inclusive: Boolean,
) {
  logcat.v(TAG) { "PopUpTo(inclusive=$inclusive) push $route - backStack=[$backStackString]" }
  val idx = indexOfLast(predicate)
  if (idx >= 0) {
    val removeFrom = if (inclusive) idx else idx + 1
    if (removeFrom < size) subList(removeFrom, size).clear()
  }
  add(route)
}

private val SnapshotStateList<NavKey>.backStackString
  get() = joinToString { it.toString() }

private const val TAG = "Navigation"
