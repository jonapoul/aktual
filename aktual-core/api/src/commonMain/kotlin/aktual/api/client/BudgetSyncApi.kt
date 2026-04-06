package aktual.api.client

import aktual.budget.model.SyncResponse
import aktual.core.model.ServerUrl
import aktual.core.model.Token
import okio.ByteString

interface BudgetSyncApi {
  suspend fun syncBudget(token: Token, requestBody: ByteString): SyncResponse

  fun interface Factory {
    fun create(serverUrl: ServerUrl): BudgetSyncApi
  }
}
