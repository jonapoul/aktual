package aktual.budget.demo

import aktual.di.BudgetGraph

interface DemoBudget {
  suspend fun open(): BudgetGraph

  suspend fun close()
}
