package aktual.core.nav

import aktual.core.model.AppCloser
import androidx.compose.runtime.Stable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.StateObject
import androidx.navigation3.runtime.NavKey
import logcat.logcat

@Stable
interface NavStack<T : NavKey> : MutableList<T>, StateObject {
  fun push(route: T)

  fun pop(): Boolean

  fun replaceAll(route: T)

  fun log()
}

@Stable
class NavStackImpl<K : NavKey>(
  private val appCloser: AppCloser?,
  private val stack: SnapshotStateList<K>,
) : NavStack<K>, MutableList<K> by stack, StateObject by stack, RandomAccess by stack {
  private val backStackString: String
    get() = joinToString { it.toString() }

  override fun push(route: K) {
    add(route)
    logcat.v(TAG) { "Push $route - backStack=[$backStackString]" }
  }

  override fun pop(): Boolean =
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

  override fun replaceAll(route: K) {
    clear()
    add(route)
    logcat.v(TAG) { "ReplaceAll $route - backStack=[$backStackString]" }
  }

  override fun log() {
    logcat.v(TAG) { "Log backStack=[$backStackString]" }
  }

  private companion object {
    private const val TAG = "Navigation"
  }
}
