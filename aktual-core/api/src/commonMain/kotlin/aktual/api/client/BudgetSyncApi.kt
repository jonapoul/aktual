package aktual.api.client

import aktual.budget.model.BudgetId
import aktual.budget.model.SyncResponse
import aktual.core.model.Token

interface BudgetSyncApi {
  suspend fun syncBudget(token: Token, budgetId: BudgetId): SyncResponse
}
