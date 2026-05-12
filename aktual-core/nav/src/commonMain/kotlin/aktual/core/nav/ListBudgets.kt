package aktual.core.nav

import androidx.compose.runtime.Immutable
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Immutable
class ListBudgetsNavigator(private val stack: NavStack<NavKey>) {
  operator fun invoke() = stack.replaceAll(ListBudgetsNavRoute)
}

@Immutable @Serializable data object ListBudgetsNavRoute : NavKey
