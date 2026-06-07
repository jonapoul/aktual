package aktual.core.nav

import aktual.budget.model.RuleId
import aktual.budget.model.ScheduleId
import aktual.budget.model.WidgetId
import androidx.compose.runtime.Immutable

@Immutable
class TransactionsNavigator(private val stack: NavStack<BudgetNavKey>) {
  operator fun invoke() = stack.replaceAll(TransactionsNavRoute)
}

@Immutable
class ReportsListNavigator(private val stack: NavStack<BudgetNavKey>) {
  operator fun invoke() = stack.push(ReportsListNavRoute)
}

@Immutable
class ReportNavigator(private val stack: NavStack<BudgetNavKey>) {
  operator fun invoke(id: WidgetId) = stack.push(ReportNavRoute(id))
}

@Immutable
class CreateReportNavigator(private val stack: NavStack<BudgetNavKey>) {
  operator fun invoke() = stack.push(CreateReportNavRoute)
}

@Immutable
class ListRulesNavigator(private val stack: NavStack<BudgetNavKey>) {
  operator fun invoke() = stack.push(ListRulesNavRoute)
}

@Immutable
class EditRuleNavigator(private val stack: NavStack<BudgetNavKey>) {
  operator fun invoke(id: RuleId) = stack.push(EditRuleNavRoute(id))

  operator fun invoke() = stack.push(CreateRuleNavRoute)
}

@Immutable
class ListSchedulesNavigator(private val stack: NavStack<BudgetNavKey>) {
  operator fun invoke() = stack.push(ListSchedulesNavRoute)
}

@Immutable
class EditScheduleNavigator(private val stack: NavStack<BudgetNavKey>) {
  operator fun invoke(id: ScheduleId) = stack.push(EditScheduleNavRoute(id))

  operator fun invoke() = stack.push(CreateScheduleNavRoute)
}

@Immutable
class ListTagsNavigator(private val stack: NavStack<BudgetNavKey>) {
  operator fun invoke() = stack.push(ListTagsNavRoute)
}
