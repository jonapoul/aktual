package actual.budget.model

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
