package actual.di

import actual.core.di.AppGraph
import actual.core.di.ProviderMap
import alakazam.kotlin.core.BuildConfig
import android.app.Activity
import android.app.Service
import android.content.BroadcastReceiver
import android.content.ContentProvider
import android.content.Context
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Includes
import dev.zacsweers.metro.Multibinds
import dev.zacsweers.metro.Provides

@DependencyGraph(AppScope::class)
interface AndroidAppGraph : AppGraph {
  @Multibinds val activityProviders: ProviderMap<Activity>
  @Multibinds(allowEmpty = true) val broadcastReceiverProviders: ProviderMap<BroadcastReceiver>
  @Multibinds(allowEmpty = true) val contentProviderProviders: ProviderMap<ContentProvider>
  @Multibinds(allowEmpty = true) val serviceProviders: ProviderMap<Service>

  @DependencyGraph.Factory
  fun interface Factory {
    fun create(
      @Provides context: Context,
      @Provides buildConfig: BuildConfig,
      @Includes debug: DebugContainer,
    ): AndroidAppGraph
  }

  fun interface Holder {
    fun graph(): AndroidAppGraph
  }
}
