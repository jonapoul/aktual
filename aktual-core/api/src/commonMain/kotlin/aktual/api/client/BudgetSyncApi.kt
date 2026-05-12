package aktual.api.client

import aktual.budget.model.SyncResponse
import okio.ByteString

interface BudgetSyncApi {
  suspend fun syncBudget(requestBody: ByteString): SyncResponse
}
