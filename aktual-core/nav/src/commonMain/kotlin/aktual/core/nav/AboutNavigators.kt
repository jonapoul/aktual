package aktual.core.nav

import androidx.compose.runtime.Immutable
import androidx.navigation3.runtime.NavKey
import kotlinx.datetime.LocalDate

@Immutable
class InfoNavigator(private val stack: NavStack<NavKey>) {
  operator fun invoke() = stack.push(InfoNavRoute)
}

@Immutable
class LicensesNavigator(private val stack: NavStack<NavKey>) {
  operator fun invoke() = stack.push(LicensesNavRoute)
}

@Immutable
class SearchLicensesNavigator(private val stack: NavStack<NavKey>) {
  operator fun invoke() = stack.push(SearchLicensesNavRoute)
}

@Immutable
class ManageStorageNavigator(private val stack: NavStack<NavKey>) {
  operator fun invoke() = stack.push(ManageStorageNavRoute)
}

@Immutable
class LogsNavigator(private val stack: NavStack<NavKey>) {
  operator fun invoke() = stack.push(LogsNavRoute)
}

@Immutable
class LogDayNavigator(private val stack: NavStack<NavKey>) {
  operator fun invoke(date: LocalDate) = stack.push(LogDayNavRoute(date))
}
