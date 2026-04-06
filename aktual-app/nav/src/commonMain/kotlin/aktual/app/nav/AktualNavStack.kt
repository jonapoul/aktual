package aktual.app.nav

import aktual.core.model.AppCloser
import androidx.compose.runtime.Stable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.StateObject
import androidx.navigation3.runtime.NavKey
import logcat.logcat

@Stable
class AktualNavStack<T : NavKey>(
  private val appCloser: AppCloser?,
  private val stack: SnapshotStateList<T>,
) : MutableList<T> by stack, StateObject by stack, RandomAccess by stack {
  private val backStackString: String
    get() = joinToString { it.toString() }

  fun push(route: T) {
    add(route)
    logcat.v(TAG) { "Push $route - backStack=[$backStackString]" }
  }

  fun pop(): Boolean =
    when (size) {
      0 -> {
        logcat.v(TAG) { "Pop - empty stack? Closing anyway" }
        appCloser?.invoke()
        false
      }

      1 -> {
        logcat.v(TAG) { "Pop - closing app" }
        appCloser?.invoke()
        true
      }

      else -> {
        removeAt(lastIndex)
        logcat.v(TAG) { "Pop - backStack=[$backStackString]" }
        true
      }
    }

  fun replaceAll(route: T) {
    clear()
    add(route)
    logcat.v(TAG) { "ReplaceAll $route - backStack=[$backStackString]" }
  }

  fun log() {
    logcat.v(TAG) { "Log backStack=[$backStackString]" }
  }

  private companion object {
    private const val TAG = "Navigation"
  }
}
