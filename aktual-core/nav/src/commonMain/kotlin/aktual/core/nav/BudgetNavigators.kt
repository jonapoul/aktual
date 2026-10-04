package aktual.core.nav

import aktual.budget.model.AccountId
import aktual.budget.model.AccountSyncSource
import aktual.budget.model.DashboardPageId
import aktual.budget.model.RuleId
import aktual.budget.model.ScheduleId
import aktual.budget.model.TagId
import aktual.budget.model.WidgetId
import androidx.compose.runtime.Immutable

@Immutable
class TransactionsNavigator(private val stack: NavStack<BudgetNavKey>) {
  operator fun invoke() = stack.replaceAll(TransactionsNavRoute)

  operator fun invoke(id: TagId) = stack.push(TransactionsWithTagNavRoute(id))
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
  operator fun invoke(page: DashboardPageId) = stack.push(CreateReportNavRoute(page))
}

@Immutable
class SearchReportsNavigator(private val stack: NavStack<BudgetNavKey>) {
  operator fun invoke() = stack.push(SearchReportsNavRoute)
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
class SearchSchedulesNavigator(private val stack: NavStack<BudgetNavKey>) {
  operator fun invoke() = stack.push(SearchSchedulesNavRoute)
}

@Immutable
class ListTagsNavigator(private val stack: NavStack<BudgetNavKey>) {
  operator fun invoke() = stack.push(ListTagsNavRoute)
}

@Immutable
class EditTagNavigator(private val stack: NavStack<BudgetNavKey>) {
  operator fun invoke() = stack.push(CreateTagNavRoute)

  operator fun invoke(id: TagId) = stack.push(EditTagNavRoute(id))
}

@Immutable
class SearchTagsNavigator(private val stack: NavStack<BudgetNavKey>) {
  operator fun invoke() = stack.push(SearchTagsNavRoute)
}

@Immutable
class BankSyncNavigator(private val stack: NavStack<BudgetNavKey>) {
  operator fun invoke() = stack.push(BankSyncNavRoute)
}

@Immutable
class BankSyncSettingsNavigator(private val stack: NavStack<BudgetNavKey>) {
  operator fun invoke(id: AccountId) = stack.push(BankSyncSettingsNavRoute(id))
}

@Immutable
class LinkBankAccountNavigator(private val stack: NavStack<BudgetNavKey>) {
  operator fun invoke(id: AccountId) = stack.push(LinkBankAccountNavRoute(id))

  fun addAccount() = stack.push(LinkBankAccountNavRoute(id = null))
}

@Immutable
class BankSyncProvidersNavigator(private val stack: NavStack<BudgetNavKey>) {
  operator fun invoke() = stack.push(BankSyncProvidersNavRoute)
}

@Immutable
class BankSyncProviderSetupNavigator(private val stack: NavStack<BudgetNavKey>) {
  operator fun invoke(source: AccountSyncSource) =
    stack.push(BankSyncProviderSetupNavRoute(source.value))
}
