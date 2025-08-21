package actual.budget.reports.ui

import actual.budget.model.BudgetId
import actual.budget.model.WidgetId
import actual.core.model.LoginToken
import androidx.compose.runtime.Immutable

@Immutable
interface ChooseReportTypeNavigator {
  fun back(): Boolean
  fun toReport(token: LoginToken, budgetId: BudgetId, widgetId: WidgetId)
}
