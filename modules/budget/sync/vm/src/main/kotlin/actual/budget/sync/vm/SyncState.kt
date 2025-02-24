package actual.budget.sync.vm

import androidx.compose.runtime.Immutable

@Immutable
sealed interface SyncState {
  data object TalkingToServer : SyncState

  data class Downloading(
    val size: Bytes,
    val percent: Percent,
  ) : SyncState

  data object Validating : SyncState

  data object Success : SyncState

  data class Failure(
    val reason: String?,
  ) : SyncState
}
