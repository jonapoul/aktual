package aktual.app.nav

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation3.runtime.NavKey

@Immutable
fun interface BackNavigator {
  operator fun invoke()
}

fun BackNavigator(stack: SnapshotStateList<NavKey>) = BackNavigator { stack.debugPop() }
