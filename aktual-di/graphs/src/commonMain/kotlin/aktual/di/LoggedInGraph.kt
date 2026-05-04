package aktual.di

import aktual.core.model.Token
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.GraphExtension
import dev.zacsweers.metro.Provides

@GraphExtension(LoggedInScope::class)
interface LoggedInGraph : AktualGraph {
  val budgetGraphFactory: BudgetGraph.Factory

  @GraphExtension.Factory
  @ContributesTo(ServerChosenScope::class)
  fun interface Factory {
    fun create(@Provides token: Token): LoggedInGraph
  }
}
