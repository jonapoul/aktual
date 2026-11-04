package aktual.api.client

import aktual.budget.model.BudgetId
import aktual.budget.model.SyncResponse
import okio.ByteString

interface BudgetSyncApi {
  // Throws SyncRejectedException if the server refuses the sync request
  suspend fun syncBudget(requestBody: ByteString): SyncResponse

  suspend fun renameBudget(id: BudgetId, name: String)
}

// The sync server rejected the messages with a 400, keeping none of them. See
// packages/sync-server/src/sync-simple.js upstream
class SyncRejectedException(val reason: String, cause: Throwable? = null) :
  Exception("Sync rejected: $reason", cause) {
  // A message was timestamped too far ahead of the server's clock
  val isClockDrift: Boolean
    get() = reason == "clock-drift"
}
