package aktual.di

import aktual.core.model.Token
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.ForScope
import dev.zacsweers.metro.GraphExtension
import dev.zacsweers.metro.Multibinds
import dev.zacsweers.metro.Provides

@GraphExtension(LoggedInScope::class)
interface LoggedInGraph : AktualGraph {
  val budgetGraphFactory: BudgetGraph.Factory

  @Multibinds(allowEmpty = true)
  @ForScope(LoggedInScope::class)
  val loggedInCloseables: Set<Closeable>

  @Multibinds(allowEmpty = true)
  @ForScope(LoggedInScope::class)
  val loggedInInitializables: Set<Initializable>

  override val closeables: Set<Closeable>
    get() = loggedInCloseables

  override val initializables: Set<Initializable>
    get() = loggedInInitializables

  override val coroutineScope: LoggedInCoroutineScope

  @GraphExtension.Factory
  @ContributesTo(ServerChosenScope::class)
  fun interface Factory {
    fun create(@Provides token: Token): LoggedInGraph
  }
}
