package actual.budget.list.vm

import androidx.compose.runtime.Immutable

@Immutable
enum class BudgetState {
  Local,
  Remote,
  Synced,
  Syncing,
  Detached,
  Broken,
  Unknown,
}
