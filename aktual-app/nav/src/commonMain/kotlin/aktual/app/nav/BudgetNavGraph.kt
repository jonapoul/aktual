package aktual.app.nav

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.GraphExtension

@GraphExtension(BudgetNavScope::class)
interface BudgetNavGraph {
  val budgetNavEntryContributors: Set<BudgetNavEntryContributor>

  @GraphExtension.Factory
  @ContributesTo(AppScope::class)
  fun interface Factory {
    fun createBudgetNavGraph(): BudgetNavGraph
  }
}
