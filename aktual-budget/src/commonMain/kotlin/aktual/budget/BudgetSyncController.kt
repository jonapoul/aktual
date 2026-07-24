package aktual.budget

import aktual.budget.model.LocalChange

interface BudgetSyncController {
  suspend fun syncChanges(vararg changes: LocalChange) = syncChanges(changes.toList())

  suspend fun syncChanges(changes: List<LocalChange>)

  fun schedule()
}
