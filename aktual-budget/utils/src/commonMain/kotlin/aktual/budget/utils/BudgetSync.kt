package aktual.budget.utils

import aktual.budget.model.LocalChange

interface BudgetSync {
  suspend operator fun invoke(vararg changes: LocalChange) = invoke(changes.toList())

  suspend operator fun invoke(changes: List<LocalChange>)

  fun schedule()
}
