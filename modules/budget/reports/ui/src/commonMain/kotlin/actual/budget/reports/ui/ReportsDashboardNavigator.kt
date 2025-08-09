package actual.budget.reports.ui

import actual.account.model.LoginToken
import actual.budget.model.BudgetId
import actual.budget.model.WidgetId
import androidx.compose.runtime.Immutable

@Immutable
interface ReportsDashboardNavigator {
  fun back(): Boolean
  fun toReport(token: LoginToken, budgetId: BudgetId, widgetId: WidgetId)
  fun createReport(token: LoginToken, budgetId: BudgetId)
}
