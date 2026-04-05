package aktual.app.nav

import aktual.budget.model.BudgetId
import aktual.core.model.Token
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Immutable
class BudgetNavRailNavigator(private val stack: SnapshotStateList<NavKey>) {
  operator fun invoke(token: Token, budgetId: BudgetId) =
    stack.debugReplaceAll(BudgetNavRailNavRoute(token, budgetId))
}

@Immutable
@Serializable
data class BudgetNavRailNavRoute(val token: Token, val budgetId: BudgetId) : NavKey
