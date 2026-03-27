package aktual.app.nav

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation3.runtime.NavKey

@Immutable
class BackNavigator(private val stack: SnapshotStateList<NavKey>) {
  operator fun invoke(): Boolean = stack.debugPop()
}
