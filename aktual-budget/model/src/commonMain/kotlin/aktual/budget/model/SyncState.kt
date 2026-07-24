package aktual.budget.model

import androidx.compose.runtime.Immutable

@Immutable
sealed interface SyncState {
  data object Inactive : SyncState

  data object Syncing : SyncState

  sealed interface Error : SyncState

  data object NoToken : Error

  data class SyncFailed(val cause: String) : Error
}
