package aktual.test

import aktual.budget.BudgetFiles
import aktual.budget.demo.DemoBudget
import aktual.core.nav.NavEntryContributor
import aktual.di.AppGraph
import aktual.di.RunLevelController
import aktual.di.RunLevelState
import dev.zacsweers.metro.Multibinds

interface TestAppGraph : AppGraph {
  val runLevelController: RunLevelController
  val runLevelState: RunLevelState
  val demoBudget: DemoBudget
  val budgetFiles: BudgetFiles

  // No UI modules in tests so no contributors are registered - only here to fix build
  @Suppress("unused")
  @Multibinds(allowEmpty = true)
  val navEntryContributors: Set<NavEntryContributor>
}
