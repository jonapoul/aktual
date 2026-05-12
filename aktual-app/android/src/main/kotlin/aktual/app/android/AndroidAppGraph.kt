package aktual.app.android

import aktual.core.model.BuildConfig
import aktual.di.AppGraph
import aktual.di.AppScope
import aktual.di.RunLevelInitialiser
import android.content.Context
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metrox.android.MetroAppComponentProviders

@DependencyGraph(AppScope::class)
interface AndroidAppGraph : AppGraph, MetroAppComponentProviders {
  val buildConfig: BuildConfig
  val initialiser: RunLevelInitialiser

  @DependencyGraph.Factory
  fun interface Factory {
    fun create(@Provides context: Context): AndroidAppGraph
  }
}
