package aktual.app.nav

import androidx.compose.runtime.Immutable
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Immutable
class InfoNavigator(private val stack: AktualNavStack<NavKey>) {
  operator fun invoke() = stack.push(InfoNavRoute)
}

@Immutable @Serializable data object InfoNavRoute : NavKey

@Immutable
class LicensesNavigator(private val stack: AktualNavStack<NavKey>) {
  operator fun invoke() = stack.push(LicensesNavRoute)
}

@Immutable @Serializable data object LicensesNavRoute : NavKey

@Immutable
class ManageStorageNavigator(private val stack: AktualNavStack<NavKey>) {
  operator fun invoke() = stack.push(ManageStorageNavRoute)
}

@Immutable @Serializable data object ManageStorageNavRoute : NavKey
