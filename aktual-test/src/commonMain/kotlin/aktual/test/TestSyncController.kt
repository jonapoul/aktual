package aktual.test

import aktual.budget.model.LocalChange
import aktual.budget.utils.BudgetSyncController

class TestSyncController : BudgetSyncController {
  val changes = mutableListOf<LocalChange>()

  override suspend fun syncChanges(changes: List<LocalChange>) {
    this.changes += changes
  }

  override fun schedule() = Unit
}
