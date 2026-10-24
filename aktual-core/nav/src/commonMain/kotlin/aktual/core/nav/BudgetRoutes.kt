package aktual.core.nav

import aktual.budget.model.AccountGroup
import aktual.budget.model.AccountId
import aktual.budget.model.CategoryId
import aktual.budget.model.ConditionsFilter
import aktual.budget.model.DashboardPageId
import aktual.budget.model.RuleId
import aktual.budget.model.ScheduleId
import aktual.budget.model.TagId
import aktual.budget.model.TransactionId
import aktual.budget.model.WidgetId
import kotlinx.datetime.YearMonth
import kotlinx.serialization.Serializable

// Routes with args must be data classes, not value classes. The nav stack is saved as JSON of the
// sealed BudgetNavKey type, and a value class serializes as its bare value with no type
// discriminator, so restoring it (e.g. after rotation) crashes

@Serializable data object HomeNavRoute : BudgetNavKey.Home

// A null month shows the current one
@Serializable data class BudgetNavRoute(val month: YearMonth? = null) : BudgetNavKey.Budget

// One category's history, with its transactions in the selected month
@Serializable
data class BudgetCategoryNavRoute(val category: CategoryId, val month: YearMonth) :
  BudgetNavKey.Budget

@Serializable data object TransactionsNavRoute : BudgetNavKey.Transactions

@Serializable data class TransactionsWithTagNavRoute(val id: TagId) : BudgetNavKey.Transactions

@Serializable data class AccountTransactionsNavRoute(val id: AccountId) : BudgetNavKey.Transactions

@Serializable data object CreateAccountNavRoute : BudgetNavKey.Transactions

@Serializable
data class AccountGroupTransactionsNavRoute(val group: AccountGroup) : BudgetNavKey.Transactions

@Serializable data object UncategorisedTransactionsNavRoute : BudgetNavKey.Transactions

@Serializable
data class CategoryTransactionsNavRoute(val category: CategoryId, val month: YearMonth) :
  BudgetNavKey.Transactions

// A report's group or interval, drilled down to
@Serializable
data class FilteredTransactionsNavRoute(val title: String, val filter: ConditionsFilter) :
  BudgetNavKey.Transactions

@Serializable data class TransactionNavRoute(val id: TransactionId) : BudgetNavKey.Transactions

// Starts the new transaction in the account or category of the list it was added from
@Serializable
data class CreateTransactionNavRoute(
  val account: AccountId? = null,
  val category: CategoryId? = null,
) : BudgetNavKey.Transactions

@Serializable data object ReportsListNavRoute : BudgetNavKey.Reports

@Serializable data class ReportNavRoute(val id: WidgetId) : BudgetNavKey.Reports

@Serializable data class CreateReportNavRoute(val page: DashboardPageId) : BudgetNavKey.Reports

@Serializable data class NewCustomReportNavRoute(val page: DashboardPageId) : BudgetNavKey.Reports

@Serializable data object SearchReportsNavRoute : BudgetNavKey.Reports

@Serializable data object ListRulesNavRoute : BudgetNavKey.Rules

@Serializable data class EditRuleNavRoute(val id: RuleId) : BudgetNavKey.Rules

@Serializable data object CreateRuleNavRoute : BudgetNavKey.Rules

@Serializable data object ListSchedulesNavRoute : BudgetNavKey.Schedules

@Serializable data object CreateScheduleNavRoute : BudgetNavKey.Schedules

@Serializable data object SearchSchedulesNavRoute : BudgetNavKey.Schedules

@Serializable data class EditScheduleNavRoute(val id: ScheduleId) : BudgetNavKey.Schedules

@Serializable data object ListTagsNavRoute : BudgetNavKey.Tags

@Serializable data object CreateTagNavRoute : BudgetNavKey.Tags

@Serializable data object SearchTagsNavRoute : BudgetNavKey.Tags

@Serializable data class EditTagNavRoute(val id: TagId) : BudgetNavKey.Tags

@Serializable data object BankSyncNavRoute : BudgetNavKey.BankSync

@Serializable data class BankSyncSettingsNavRoute(val id: AccountId) : BudgetNavKey.BankSync

@Serializable data class LinkBankAccountNavRoute(val id: AccountId?) : BudgetNavKey.BankSync

@Serializable data object BankSyncProvidersNavRoute : BudgetNavKey.BankSync

@Serializable data class BankSyncProviderSetupNavRoute(val source: String) : BudgetNavKey.BankSync
