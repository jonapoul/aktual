package aktual.budget.model

import alakazam.kotlin.core.StateHolder
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

sealed interface SyncState {
  data object Inactive : SyncState
  data object Queued : SyncState
  data object InProgress : SyncState

  data object Success : SyncState

  sealed interface Failed : SyncState
  data object Disconnected : Failed
  data class OtherFailure(val cause: String) : Failed
}

@Inject
@SingleIn(AppScope::class)
class SyncStateHolder : StateHolder<SyncState>(SyncState.Inactive)
