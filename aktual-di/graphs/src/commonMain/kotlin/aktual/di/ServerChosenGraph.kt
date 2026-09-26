package aktual.di

import aktual.core.model.ServerUrl
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.ForScope
import dev.zacsweers.metro.GraphExtension
import dev.zacsweers.metro.Multibinds
import dev.zacsweers.metro.Provides

@GraphExtension(ServerChosenScope::class)
interface ServerChosenGraph : AktualGraph {
  val loggedInGraphFactory: LoggedInGraph.Factory

  @Multibinds(allowEmpty = true)
  @ForScope(ServerChosenScope::class)
  val serverChosenCloseables: Set<Closeable>

  @Multibinds(allowEmpty = true)
  @ForScope(ServerChosenScope::class)
  val serverChosenInitializables: Set<Initializable>

  override val closeables: Set<Closeable>
    get() = serverChosenCloseables

  override val initializables: Set<Initializable>
    get() = serverChosenInitializables

  override val coroutineScope: ServerChosenCoroutineScope

  @GraphExtension.Factory
  @ContributesTo(AppScope::class)
  fun interface Factory {
    fun create(@Provides url: ServerUrl): ServerChosenGraph
  }
}
