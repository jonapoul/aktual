package aktual.budget

import aktual.di.BudgetScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

enum class SyncOutcome {
  Succeeded,
  Failed,
}

// Upstream's "sync" events with a type of success, error or unauthorized. Replays the latest one so
// a collector that starts after the first sync still sees it
@Inject
@SingleIn(BudgetScope::class)
class SyncEvents {
  private val mutableCompleted =
    MutableSharedFlow<SyncOutcome>(replay = 1, onBufferOverflow = DROP_OLDEST)

  val completed: SharedFlow<SyncOutcome> = mutableCompleted.asSharedFlow()

  fun complete(outcome: SyncOutcome) {
    mutableCompleted.tryEmit(outcome)
  }
}
