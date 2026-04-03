package aktual.test

import aktual.core.model.AppGraph
import aktual.budget.di.BudgetGraph
import aktual.budget.di.BudgetGraphHolder

interface TestAppGraph : AppGraph {
  val budgetGraphBuilder: BudgetGraph.Builder
  val budgetGraphHolder: BudgetGraphHolder
}
