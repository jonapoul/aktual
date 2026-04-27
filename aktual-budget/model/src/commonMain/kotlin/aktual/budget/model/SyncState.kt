package aktual.budget.model

import alakazam.kotlin.StateHolder
import androidx.compose.runtime.Immutable
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@Immutable
sealed interface SyncState {
  data object Inactive : SyncState

  data object Syncing : SyncState

  sealed interface Error : SyncState

  data object NoToken : Error

  data class SyncFailed(val cause: String) : Error
}

@Inject @SingleIn(AppScope::class) class SyncStateHolder : StateHolder<SyncState>(Inactive)
