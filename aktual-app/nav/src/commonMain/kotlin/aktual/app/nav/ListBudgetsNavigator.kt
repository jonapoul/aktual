package aktual.app.nav

import aktual.core.model.Token
import androidx.compose.runtime.Immutable
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Immutable
class ListBudgetsNavigator(private val stack: AktualNavStack<NavKey>) {
  operator fun invoke(token: Token) = stack.replaceAll(ListBudgetsNavRoute(token))
}

@Immutable @Serializable data class ListBudgetsNavRoute(val token: Token) : NavKey
