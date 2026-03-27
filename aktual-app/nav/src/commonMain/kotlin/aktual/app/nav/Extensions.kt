package aktual.app.nav

import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation3.runtime.NavKey
import logcat.logcat

internal fun SnapshotStateList<NavKey>.debugPush(route: NavKey) {
  logcat.v(TAG) { "Push $route - backStack=[$backStackString]" }
  add(route)
}

fun SnapshotStateList<NavKey>.debugPop(): Boolean {
  logcat.v(TAG) { "Pop - backStack=[$backStackString]" }
  if (size <= 1) return false
  removeAt(lastIndex)
  return true
}

internal fun SnapshotStateList<NavKey>.debugReplaceAll(route: NavKey) {
  logcat.v(TAG) { "ReplaceAll $route - backStack=[$backStackString]" }
  clear()
  add(route)
}

private val SnapshotStateList<NavKey>.backStackString: String
  get() = joinToString { it.toString() }

private const val TAG = "Navigation"
