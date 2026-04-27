package aktual.app.nav

import aktual.budget.model.BudgetId
import aktual.budget.model.RuleId
import aktual.budget.model.WidgetId
import aktual.core.model.Token
import androidx.compose.runtime.Immutable
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Immutable @Serializable sealed interface BudgetNavKey : NavKey

@Immutable
class TransactionsNavigator(private val stack: AktualNavStack<BudgetNavKey>) {
  operator fun invoke(token: Token, budgetId: BudgetId) =
    stack.replaceAll(TransactionsNavRoute(token, budgetId))
}

@Immutable
@Serializable
data class TransactionsNavRoute(val token: Token, val budgetId: BudgetId) : BudgetNavKey

@Suppress("unused")
@Immutable
class ReportsListNavigator(private val stack: AktualNavStack<BudgetNavKey>) {
  operator fun invoke(token: Token, budgetId: BudgetId) =
    stack.push(ReportsListNavRoute(token, budgetId))
}

@Immutable
@Serializable
data class ReportsListNavRoute(val token: Token, val budgetId: BudgetId) : BudgetNavKey

@Immutable
class ReportNavigator(private val stack: AktualNavStack<BudgetNavKey>) {
  operator fun invoke(token: Token, budgetId: BudgetId, widgetId: WidgetId) =
    stack.push(ReportNavRoute(token, budgetId, widgetId))
}

@Immutable
@Serializable
data class ReportNavRoute(val token: Token, val budgetId: BudgetId, val widgetId: WidgetId) :
  BudgetNavKey

@Immutable
class CreateReportNavigator(private val stack: AktualNavStack<BudgetNavKey>) {
  operator fun invoke(token: Token, budgetId: BudgetId) =
    stack.push(CreateReportNavRoute(token, budgetId))
}

@Immutable
@Serializable
data class CreateReportNavRoute(val token: Token, val budgetId: BudgetId) : BudgetNavKey

@Immutable
class ListRulesNavigator(private val stack: AktualNavStack<BudgetNavKey>) {
  operator fun invoke() = stack.push(ListRulesNavRoute)
}

@Immutable @Serializable data object ListRulesNavRoute : BudgetNavKey

@Immutable
class EditRuleNavigator(private val stack: AktualNavStack<BudgetNavKey>) {
  operator fun invoke(id: RuleId) = stack.push(EditRuleNavRoute(id))

  operator fun invoke() = stack.push(CreateRuleNavRoute)
}

@Immutable @Serializable @JvmInline value class EditRuleNavRoute(val id: RuleId) : BudgetNavKey

@Immutable @Serializable data object CreateRuleNavRoute : BudgetNavKey
