package aktual.core.nav

import androidx.compose.runtime.Immutable
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Immutable @Serializable data object BudgetNavRailNavRoute : NavKey

@Immutable
class BudgetNavRailNavigator(private val stack: NavStack<NavKey>) {
  operator fun invoke() = stack.replaceAll(BudgetNavRailNavRoute)
}
