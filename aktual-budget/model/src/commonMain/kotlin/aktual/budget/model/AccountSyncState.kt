@file:Suppress("FunctionName")

package aktual.budget.model

import androidx.compose.runtime.Immutable
import kotlin.time.Instant

@Immutable
sealed interface AccountSyncState {
  data object NotLinked : AccountSyncState

  data class Ok(val lastSync: Instant?) : AccountSyncState

  data class Failed(val status: BankSyncStatus) : AccountSyncState
}

// Upstream treats an account as linked if it has a sync source, and unlinking clears both
fun AccountSyncState(
  source: AccountSyncSource?,
  remoteId: String?,
  status: BankSyncStatus?,
  lastSync: Instant?,
): AccountSyncState =
  when {
    source == null || remoteId == null -> NotLinked
    status != null && status.isFailure -> AccountSyncState.Failed(status)
    else -> AccountSyncState.Ok(lastSync)
  }
