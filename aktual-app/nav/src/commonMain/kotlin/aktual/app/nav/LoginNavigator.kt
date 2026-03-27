package aktual.app.nav

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Immutable
class LoginNavigator(private val stack: SnapshotStateList<NavKey>) {
  operator fun invoke() = stack.debugPush(LoginNavRoute)
}

@Immutable @Serializable data object LoginNavRoute : NavKey
