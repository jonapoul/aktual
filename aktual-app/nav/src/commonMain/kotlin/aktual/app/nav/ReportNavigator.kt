package aktual.app.nav

import aktual.budget.model.BudgetId
import aktual.budget.model.WidgetId
import aktual.core.model.Token
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Immutable
class ReportNavigator(private val stack: SnapshotStateList<NavKey>) {
  operator fun invoke(token: Token, budgetId: BudgetId, widgetId: WidgetId) =
    stack.debugPush(ReportNavRoute(token, budgetId, widgetId))
}

@Immutable
@Serializable
data class ReportNavRoute(val token: Token, val budgetId: BudgetId, val widgetId: WidgetId) : NavKey
