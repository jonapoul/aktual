package aktual.api.client

import aktual.budget.model.BudgetId
import aktual.budget.model.SyncResponse
import okio.ByteString

interface BudgetSyncApi {
  // Throws SyncRejectedException if the server refuses the sync request
  suspend fun syncBudget(requestBody: ByteString): SyncResponse

  suspend fun renameBudget(id: BudgetId, name: String)

  // Uploads a zip of the budget, returning its group ID. Throws UploadRejectedException if the
  // server refuses it
  suspend fun uploadBudget(id: BudgetId, name: String, groupId: String?, zip: ByteArray): String
}

// The sync server rejected the messages with a 400, keeping none of them. See
// packages/sync-server/src/sync-simple.js upstream
class SyncRejectedException(val reason: String, cause: Throwable? = null) :
  Exception("Sync rejected: $reason", cause) {
  // A message was timestamped too far ahead of the server's clock
  val isClockDrift: Boolean
    get() = reason == "clock-drift"
}

// See the upload-user-file handler in packages/sync-server/src/app-sync.ts upstream
class UploadRejectedException(val reason: String, cause: Throwable? = null) :
  Exception("Upload rejected: $reason", cause)
