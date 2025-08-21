package actual.budget.sync.ui

import actual.budget.model.BudgetId
import actual.core.model.LoginToken
import androidx.compose.runtime.Immutable

@Immutable
interface SyncBudgetNavigator {
  fun back(): Boolean
  fun toBudget(token: LoginToken, budgetId: BudgetId)
}
