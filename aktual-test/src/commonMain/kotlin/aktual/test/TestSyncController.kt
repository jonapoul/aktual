package aktual.test

import aktual.budget.BudgetSyncController
import aktual.budget.model.LocalChange

class TestSyncController : BudgetSyncController {
  val changes = mutableListOf<LocalChange>()

  override suspend fun syncChanges(changes: List<LocalChange>) {
    this.changes += changes
  }

  override fun schedule() = Unit
}
