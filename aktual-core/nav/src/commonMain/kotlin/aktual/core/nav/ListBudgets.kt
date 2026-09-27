package aktual.core.nav

import androidx.compose.runtime.Immutable
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Immutable
class ListBudgetsNavigator(private val stack: NavStack<NavKey>) {
  operator fun invoke() = stack.replaceAll(ListBudgetsNavRoute)
}

@Serializable data object ListBudgetsNavRoute : NavKey
