package aktual.test

import aktual.budget.model.LocalChange
import aktual.budget.utils.BudgetSync

class TestBudgetSync : BudgetSync {
  val changes = mutableListOf<LocalChange>()

  override suspend fun invoke(changes: List<LocalChange>) {
    this.changes += changes
  }

  override fun schedule() = Unit
}
