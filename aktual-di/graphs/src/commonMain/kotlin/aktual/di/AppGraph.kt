package aktual.di

import dev.zacsweers.metro.ForScope
import dev.zacsweers.metro.Multibinds

interface AppGraph : AktualGraph {
  val serverChosenGraphFactory: ServerChosenGraph.Factory

  override val coroutineScope: AppCoroutineScope

  @Multibinds(allowEmpty = true) @ForScope(AppScope::class) val appCloseables: Set<Closeable>

  @Multibinds(allowEmpty = true)
  @ForScope(AppScope::class)
  val appInitializables: Set<Initializable>

  override val closeables: Set<Closeable>
    get() = appCloseables

  override val initializables: Set<Initializable>
    get() = appInitializables
}
