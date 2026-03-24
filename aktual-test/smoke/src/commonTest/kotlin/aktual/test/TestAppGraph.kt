package aktual.test

import aktual.core.di.AppGraph
import aktual.core.di.BudgetGraph
import aktual.core.di.BudgetGraphHolder

interface TestAppGraph : AppGraph {
  val budgetGraphBuilder: BudgetGraph.Builder
  val budgetGraphHolder: BudgetGraphHolder
}
