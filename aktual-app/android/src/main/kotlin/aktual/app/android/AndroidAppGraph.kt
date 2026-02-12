package aktual.app.android

import aktual.core.di.AppGraph
import aktual.core.model.BuildConfig
import android.content.Context
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metrox.android.MetroAppComponentProviders

@DependencyGraph(AppScope::class)
interface AndroidAppGraph : AppGraph, MetroAppComponentProviders {
  val buildConfig: BuildConfig

  @DependencyGraph.Factory
  fun interface Factory {
    fun create(@Provides context: Context): AndroidAppGraph
  }
}
