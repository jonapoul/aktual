package aktual.test

import aktual.budget.di.BudgetGraph
import aktual.budget.di.BudgetGraphHolder
import aktual.core.model.AppGraph

interface TestAppGraph : AppGraph {
  val budgetGraphBuilder: BudgetGraph.Builder
  val budgetGraphHolder: BudgetGraphHolder
}
