package aktual.app.nav

import aktual.budget.model.BudgetId
import aktual.budget.model.WidgetId
import aktual.core.model.ThemeId
import aktual.core.model.Token
import androidx.compose.runtime.Immutable
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Immutable @Serializable data object ChangePasswordNavRoute : NavKey

@Immutable @Serializable data object InfoNavRoute : NavKey

@Immutable @Serializable data object LicensesNavRoute : NavKey

@Immutable @Serializable data object MetricsNavRoute : NavKey

@Immutable @Serializable data class ListBudgetsNavRoute(val token: Token) : NavKey

@Immutable @Serializable data object LoginNavRoute : NavKey

@Immutable @Serializable data object ServerUrlNavRoute : NavKey

@Immutable @Serializable data object SettingsNavRoute : NavKey

@Immutable @Serializable data object ThemeSettingsNavRoute : NavKey

@Immutable @Serializable data class InspectThemeNavRoute(val id: ThemeId) : NavKey

@Immutable
@Serializable
data class TransactionsNavRoute(val token: Token, val budgetId: BudgetId) : NavKey

@Immutable
@Serializable
data class ReportsListNavRoute(val token: Token, val budgetId: BudgetId) : NavKey

@Immutable
@Serializable
data class ReportNavRoute(val token: Token, val budgetId: BudgetId, val widgetId: WidgetId) : NavKey

@Immutable
@Serializable
data class CreateReportNavRoute(val token: Token, val budgetId: BudgetId) : NavKey
