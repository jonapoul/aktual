package aktual.app.nav

import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation3.runtime.NavKey
import logcat.logcat

fun <T : NavKey> SnapshotStateList<T>.debugPush(route: T) {
  logcat.v(TAG) { "Push $route - backStack=[$backStackString]" }
  add(route)
}

fun <T : NavKey> SnapshotStateList<T>.debugPop(): Boolean {
  logcat.v(TAG) { "Pop - backStack=[$backStackString]" }
  if (size <= 1) return false
  removeAt(lastIndex)
  return true
}

fun <T : NavKey> SnapshotStateList<T>.debugReplaceAll(route: T) {
  logcat.v(TAG) { "ReplaceAll $route - backStack=[$backStackString]" }
  clear()
  add(route)
}

private val <T : NavKey> SnapshotStateList<T>.backStackString: String
  get() = joinToString { it.toString() }

private const val TAG = "Navigation"
