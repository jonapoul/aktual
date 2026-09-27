package aktual.core.nav

import aktual.budget.model.DashboardPageId
import aktual.budget.model.RuleId
import aktual.budget.model.ScheduleId
import aktual.budget.model.TagId
import aktual.budget.model.WidgetId
import kotlinx.serialization.Serializable

@Serializable data object TransactionsNavRoute : BudgetNavKey.Transactions

@Serializable
@JvmInline
value class TransactionsWithTagNavRoute(val id: TagId) : BudgetNavKey.Transactions

@Serializable data object ReportsListNavRoute : BudgetNavKey.Reports

@JvmInline @Serializable value class ReportNavRoute(val id: WidgetId) : BudgetNavKey.Reports

@JvmInline
@Serializable
value class CreateReportNavRoute(val page: DashboardPageId) : BudgetNavKey.Reports

@Serializable data object SearchReportsNavRoute : BudgetNavKey.Reports

@Serializable data object ListRulesNavRoute : BudgetNavKey.Rules

@JvmInline @Serializable value class EditRuleNavRoute(val id: RuleId) : BudgetNavKey.Rules

@Serializable data object CreateRuleNavRoute : BudgetNavKey.Rules

@Serializable data object ListSchedulesNavRoute : BudgetNavKey.Schedules

@Serializable data object CreateScheduleNavRoute : BudgetNavKey.Schedules

@JvmInline
@Serializable
value class EditScheduleNavRoute(val id: ScheduleId) : BudgetNavKey.Schedules

@Serializable data object ListTagsNavRoute : BudgetNavKey.Tags

@Serializable data object CreateTagNavRoute : BudgetNavKey.Tags

@Serializable data object SearchTagsNavRoute : BudgetNavKey.Tags

@JvmInline @Serializable value class EditTagNavRoute(val id: TagId) : BudgetNavKey.Tags
