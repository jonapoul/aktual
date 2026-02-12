package aktual.budget.reports.ui

import aktual.budget.model.BudgetId
import aktual.budget.model.WidgetId
import aktual.core.model.Token
import androidx.compose.runtime.Immutable

@Immutable
interface ChooseReportTypeNavigator {
  fun back(): Boolean

  fun toReport(token: Token, budgetId: BudgetId, widgetId: WidgetId)
}
