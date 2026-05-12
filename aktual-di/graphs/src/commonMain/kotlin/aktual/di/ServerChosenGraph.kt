package aktual.di

import aktual.core.model.ServerUrl
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.GraphExtension
import dev.zacsweers.metro.Provides

@GraphExtension(ServerChosenScope::class)
interface ServerChosenGraph : AktualGraph {
  val loggedInGraphFactory: LoggedInGraph.Factory

  @GraphExtension.Factory
  @ContributesTo(AppScope::class)
  fun interface Factory {
    fun create(@Provides url: ServerUrl): ServerChosenGraph
  }
}
