package aktual.app.nav

import aktual.budget.model.BudgetId
import aktual.budget.model.WidgetId
import aktual.core.model.Token
import androidx.compose.runtime.Immutable
import java.io.Serializable as JSerializable
import kotlinx.serialization.Serializable

@Immutable @Serializable data object ChangePasswordNavRoute

@Immutable @Serializable data object InfoNavRoute

@Immutable @Serializable data object LicensesNavRoute

@Immutable @Serializable data object MetricsNavRoute

@Immutable
@Serializable
data class ListBudgetsNavRoute(
    val token: Token,
) : JSerializable

@Immutable @Serializable data object LoginNavRoute

@Immutable @Serializable data object ServerUrlNavRoute

@Immutable @Serializable data object SettingsNavRoute

@Immutable
@Serializable
data class SyncBudgetsNavRoute(
    val token: Token,
    val budgetId: BudgetId,
) : JSerializable

@Immutable
@Serializable
data class TransactionsNavRoute(
    val token: Token,
    val budgetId: BudgetId,
) : JSerializable

@Immutable
@Serializable
data class ReportsListNavRoute(
    val token: Token,
    val budgetId: BudgetId,
) : JSerializable

@Immutable
@Serializable
data class ReportNavRoute(
    val token: Token,
    val budgetId: BudgetId,
    val widgetId: WidgetId,
) : JSerializable

@Immutable
@Serializable
data class CreateReportNavRoute(
    val token: Token,
    val budgetId: BudgetId,
) : JSerializable
