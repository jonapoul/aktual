package aktual.budget.list.ui

import aktual.budget.model.BudgetId
import aktual.core.model.LoginToken
import androidx.compose.runtime.Immutable

@Immutable
interface ListBudgetsNavigator {
  fun toAbout()
  fun toChangePassword()
  fun toUrl()
  fun toSettings()
  fun toMetrics()
  fun toSyncBudget(token: LoginToken, id: BudgetId)
}
