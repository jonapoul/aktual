package aktual.app.nav

import aktual.budget.model.BudgetId
import aktual.budget.model.RuleId
import aktual.budget.model.ScheduleId
import aktual.budget.model.WidgetId
import aktual.core.model.Token
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Immutable
@Serializable
sealed interface BudgetNavKey : NavKey {
  val tab: BudgetTab

  @Immutable
  sealed interface Transactions : BudgetNavKey {
    override val tab: BudgetTab
      get() = BudgetTab.Transactions
  }

  @Immutable
  sealed interface Reports : BudgetNavKey {
    override val tab: BudgetTab
      get() = BudgetTab.Reports
  }

  @Immutable
  sealed interface Rules : BudgetNavKey {
    override val tab: BudgetTab
      get() = BudgetTab.Rules
  }

  @Immutable
  sealed interface Schedules : BudgetNavKey {
    override val tab: BudgetTab
      get() = BudgetTab.Schedules
  }
}

inline fun <reified K : BudgetNavKey> EntryProviderScope<BudgetNavKey>.budgetEntry(
  metadata: Map<String, Any> = emptyMap(),
  noinline content: @Composable (K) -> Unit,
) = entry<K>(clazzContentKey = { key -> key.tab }, metadata = metadata, content = content)

@Immutable
class TransactionsNavigator(private val stack: AktualNavStack<BudgetNavKey>) {
  operator fun invoke(token: Token, budgetId: BudgetId) =
    stack.replaceAll(TransactionsNavRoute(token, budgetId))
}

@Immutable
@Serializable
data class TransactionsNavRoute(val token: Token, val budgetId: BudgetId) :
  BudgetNavKey.Transactions

@Suppress("unused")
@Immutable
class ReportsListNavigator(private val stack: AktualNavStack<BudgetNavKey>) {
  operator fun invoke(token: Token, budgetId: BudgetId) =
    stack.push(ReportsListNavRoute(token, budgetId))
}

@Immutable
@Serializable
data class ReportsListNavRoute(val token: Token, val budgetId: BudgetId) : BudgetNavKey.Reports

@Immutable
class ReportNavigator(private val stack: AktualNavStack<BudgetNavKey>) {
  operator fun invoke(token: Token, budgetId: BudgetId, widgetId: WidgetId) =
    stack.push(ReportNavRoute(token, budgetId, widgetId))
}

@Immutable
@Serializable
data class ReportNavRoute(val token: Token, val budgetId: BudgetId, val widgetId: WidgetId) :
  BudgetNavKey.Reports

@Immutable
class CreateReportNavigator(private val stack: AktualNavStack<BudgetNavKey>) {
  operator fun invoke(token: Token, budgetId: BudgetId) =
    stack.push(CreateReportNavRoute(token, budgetId))
}

@Immutable
@Serializable
data class CreateReportNavRoute(val token: Token, val budgetId: BudgetId) : BudgetNavKey.Reports

@Immutable
class ListRulesNavigator(private val stack: AktualNavStack<BudgetNavKey>) {
  operator fun invoke() = stack.push(ListRulesNavRoute)
}

@Immutable @Serializable data object ListRulesNavRoute : BudgetNavKey.Rules

@Immutable
class EditRuleNavigator(private val stack: AktualNavStack<BudgetNavKey>) {
  operator fun invoke(id: RuleId) = stack.push(EditRuleNavRoute(id))

  operator fun invoke() = stack.push(CreateRuleNavRoute)
}

@Immutable
@Serializable
@JvmInline
value class EditRuleNavRoute(val id: RuleId) : BudgetNavKey.Rules

@Immutable @Serializable data object CreateRuleNavRoute : BudgetNavKey.Rules

@Immutable
class ListSchedulesNavigator(private val stack: AktualNavStack<BudgetNavKey>) {
  operator fun invoke() = stack.push(ListSchedulesNavRoute)
}

@Immutable @Serializable data object ListSchedulesNavRoute : BudgetNavKey.Schedules

@Immutable @Serializable data object CreateScheduleNavRoute : BudgetNavKey.Schedules

@JvmInline
@Immutable
@Serializable
value class EditScheduleNavRoute(val id: ScheduleId) : BudgetNavKey.Schedules

@Immutable
class EditScheduleNavigator(private val stack: AktualNavStack<BudgetNavKey>) {
  operator fun invoke(id: ScheduleId) = stack.push(EditScheduleNavRoute(id))

  operator fun invoke() = stack.push(CreateScheduleNavRoute)
}
