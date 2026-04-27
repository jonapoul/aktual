package aktual.budget.sync.domain

import kotlinx.io.IOException

sealed interface SyncResult {
  data class Success(val affectedTables: Set<String>) : SyncResult

  sealed interface Error : SyncResult

  data class NetworkError(val cause: IOException) : Error

  data object OutOfSyncError : Error

  data class MissingPrefError(val missingGroupId: Boolean, val missingBudgetId: Boolean) : Error

  data class OtherError(val cause: Throwable? = null) : Error
}
