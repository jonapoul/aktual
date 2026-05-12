package aktual.core.nav

import androidx.compose.runtime.Immutable
import androidx.navigation3.runtime.NavKey

@Immutable
class InfoNavigator(private val stack: NavStack<NavKey>) {
  operator fun invoke() = stack.push(InfoNavRoute)
}

@Immutable
class LicensesNavigator(private val stack: NavStack<NavKey>) {
  operator fun invoke() = stack.push(LicensesNavRoute)
}

@Immutable
class ManageStorageNavigator(private val stack: NavStack<NavKey>) {
  operator fun invoke() = stack.push(ManageStorageNavRoute)
}
