package actual.budget.sync.vm

import androidx.compose.runtime.Immutable

@Immutable
enum class SyncOverallState {
  NotStarted,
  InProgress,
  Failed,
  Succeeded,
}
