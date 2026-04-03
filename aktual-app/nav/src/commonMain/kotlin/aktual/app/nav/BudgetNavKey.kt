package aktual.app.nav

import aktual.budget.model.BudgetId
import aktual.budget.model.WidgetId
import aktual.core.model.Token
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Immutable @Serializable sealed interface BudgetNavKey : NavKey

@Immutable
class TransactionsNavigator(private val stack: SnapshotStateList<BudgetNavKey>) {
  operator fun invoke(token: Token, budgetId: BudgetId) =
    stack.debugReplaceAll(TransactionsNavRoute(token, budgetId))
}

@Immutable
@Serializable
data class TransactionsNavRoute(val token: Token, val budgetId: BudgetId) : BudgetNavKey

@Suppress("unused")
@Immutable
class ReportsListNavigator(private val stack: SnapshotStateList<BudgetNavKey>) {
  operator fun invoke(token: Token, budgetId: BudgetId) =
    stack.debugPush(ReportsListNavRoute(token, budgetId))
}

@Immutable
@Serializable
data class ReportsListNavRoute(val token: Token, val budgetId: BudgetId) : BudgetNavKey

@Immutable
class ReportNavigator(private val stack: SnapshotStateList<BudgetNavKey>) {
  operator fun invoke(token: Token, budgetId: BudgetId, widgetId: WidgetId) =
    stack.debugPush(ReportNavRoute(token, budgetId, widgetId))
}

@Immutable
@Serializable
data class ReportNavRoute(val token: Token, val budgetId: BudgetId, val widgetId: WidgetId) :
  BudgetNavKey

@Immutable
class CreateReportNavigator(private val stack: SnapshotStateList<BudgetNavKey>) {
  operator fun invoke(token: Token, budgetId: BudgetId) =
    stack.debugPush(CreateReportNavRoute(token, budgetId))
}

@Immutable
@Serializable
data class CreateReportNavRoute(val token: Token, val budgetId: BudgetId) : BudgetNavKey

@Immutable
class ListRulesNavigator(private val stack: SnapshotStateList<BudgetNavKey>) {
  operator fun invoke(token: Token, budgetId: BudgetId) =
    stack.debugPush(ListRulesNavRoute(token, budgetId))
}

@Immutable
@Serializable
data class ListRulesNavRoute(val token: Token, val budgetId: BudgetId) : BudgetNavKey
