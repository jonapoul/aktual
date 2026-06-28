package aktual.core.nav

import aktual.budget.model.RuleId
import aktual.budget.model.ScheduleId
import aktual.budget.model.TagId
import aktual.budget.model.WidgetId
import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable @Serializable data object TransactionsNavRoute : BudgetNavKey.Transactions

@Immutable
@Serializable
@JvmInline
value class TransactionsWithTagNavRoute(val id: TagId) : BudgetNavKey.Transactions

@Immutable @Serializable data object ReportsListNavRoute : BudgetNavKey.Reports

@JvmInline
@Immutable
@Serializable
value class ReportNavRoute(val id: WidgetId) : BudgetNavKey.Reports

@Immutable @Serializable data object CreateReportNavRoute : BudgetNavKey.Reports

@Immutable @Serializable data object ListRulesNavRoute : BudgetNavKey.Rules

@JvmInline
@Immutable
@Serializable
value class EditRuleNavRoute(val id: RuleId) : BudgetNavKey.Rules

@Immutable @Serializable data object CreateRuleNavRoute : BudgetNavKey.Rules

@Immutable @Serializable data object ListSchedulesNavRoute : BudgetNavKey.Schedules

@Immutable @Serializable data object CreateScheduleNavRoute : BudgetNavKey.Schedules

@JvmInline
@Immutable
@Serializable
value class EditScheduleNavRoute(val id: ScheduleId) : BudgetNavKey.Schedules

@Immutable @Serializable data object ListTagsNavRoute : BudgetNavKey.Tags

@Immutable @Serializable data object CreateTagNavRoute : BudgetNavKey.Tags

@JvmInline @Immutable @Serializable value class EditTagNavRoute(val id: TagId) : BudgetNavKey.Tags
