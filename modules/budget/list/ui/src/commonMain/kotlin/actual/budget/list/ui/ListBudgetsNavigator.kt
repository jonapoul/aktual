package actual.budget.list.ui

import actual.budget.model.BudgetId
import actual.core.model.LoginToken
import androidx.compose.runtime.Immutable

@Immutable
interface ListBudgetsNavigator {
  fun toAbout()
  fun toChangePassword()
  fun toUrl()
  fun toSettings()
  fun toSyncBudget(token: LoginToken, id: BudgetId)
}
