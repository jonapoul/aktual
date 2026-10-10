package aktual.api.client

import aktual.budget.model.BudgetId
import aktual.budget.model.SyncResponse
import okio.ByteString

interface BudgetSyncApi {
  suspend fun syncBudget(requestBody: ByteString): SyncResponse

  suspend fun renameBudget(id: BudgetId, name: String)
}
