package actual.app.desktop

import actual.app.di.JvmViewModelGraph
import actual.core.di.AppGraph
import actual.core.model.BuildConfig
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import kotlin.time.Clock

@DependencyGraph(AppScope::class)
interface JvmAppGraph : AppGraph, JvmViewModelGraph.Factory {
  val buildConfig: BuildConfig
  val windowPreferences: WindowStatePreferences
  val clock: Clock

  @DependencyGraph.Factory
  fun interface Factory {
    fun create(
      @Provides graphHolder: AppGraph.Holder,
    ): JvmAppGraph
  }
}
