package aktual.app.nav

import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation3.runtime.NavKey
import logcat.logcat

fun <T : NavKey> SnapshotStateList<T>.debugPush(route: T) {
  add(route)
  logcat.v(TAG) { "Push $route - backStack=[$backStackString]" }
}

fun <T : NavKey> SnapshotStateList<T>.debugPop(): Boolean {
  if (size <= 1) return false
  removeAt(lastIndex)
  logcat.v(TAG) { "Pop - backStack=[$backStackString]" }
  return true
}

fun <T : NavKey> SnapshotStateList<T>.debugReplaceAll(route: T) {
  clear()
  add(route)
  logcat.v(TAG) { "ReplaceAll $route - backStack=[$backStackString]" }
}

fun <T : NavKey> SnapshotStateList<T>.debugLog() {
  logcat.v(TAG) { "Log backStack=[$backStackString]" }
}

private val <T : NavKey> SnapshotStateList<T>.backStackString: String
  get() = joinToString { it.toString() }

private const val TAG = "Navigation"
