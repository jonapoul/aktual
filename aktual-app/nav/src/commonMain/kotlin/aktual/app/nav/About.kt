package aktual.app.nav

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Immutable
class InfoNavigator(private val stack: SnapshotStateList<NavKey>) {
  operator fun invoke() = stack.debugPush(InfoNavRoute)
}

@Immutable @Serializable data object InfoNavRoute : NavKey

@Immutable
class LicensesNavigator(private val stack: SnapshotStateList<NavKey>) {
  operator fun invoke() = stack.debugPush(LicensesNavRoute)
}

@Immutable @Serializable data object LicensesNavRoute : NavKey

@Immutable
class ManageStorageNavigator(private val stack: SnapshotStateList<NavKey>) {
  operator fun invoke() = stack.debugPush(ManageStorageNavRoute)
}

@Immutable @Serializable data object ManageStorageNavRoute : NavKey
