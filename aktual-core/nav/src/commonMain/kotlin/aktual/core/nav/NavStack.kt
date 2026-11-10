package aktual.core.nav

import aktual.core.utils.AppCloser
import aktual.log
import androidx.compose.runtime.Stable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.StateObject
import androidx.navigation3.runtime.NavKey

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
    log.v(TAG) { "Push $route - backStack=[$backStackString]" }
  }

  override fun pop(): Boolean =
    when (size) {
      0 -> {
        log.v(TAG) { "Pop - empty stack? Closing anyway" }
        appCloser?.invoke()
        false
      }

      1 -> {
        log.v(TAG) { "Pop - closing app" }
        appCloser?.invoke()
        true
      }

      else -> {
        removeAt(lastIndex)
        log.v(TAG) { "Pop - backStack=[$backStackString]" }
        true
      }
    }

  override fun replaceAll(route: K) {
    clear()
    add(route)
    log.v(TAG) { "ReplaceAll $route - backStack=[$backStackString]" }
  }

  override fun log() {
    log.v(TAG) { "Log backStack=[$backStackString]" }
  }

  private companion object {
    private const val TAG = "Navigation"
  }
}
