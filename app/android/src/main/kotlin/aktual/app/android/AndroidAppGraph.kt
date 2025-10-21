/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.app.android

import aktual.app.di.AndroidViewModelGraph
import aktual.core.di.AppGraph
import aktual.core.di.ProviderMap
import aktual.core.model.BuildConfig
import android.app.Activity
import android.app.Service
import android.content.BroadcastReceiver
import android.content.ContentProvider
import android.content.Context
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Multibinds
import dev.zacsweers.metro.Provides

@DependencyGraph(AppScope::class)
interface AndroidAppGraph : AppGraph, AndroidViewModelGraph.Factory {
  @Multibinds val activityProviders: ProviderMap<Activity>
  @Multibinds(allowEmpty = true) val broadcastReceiverProviders: ProviderMap<BroadcastReceiver>
  @Multibinds(allowEmpty = true) val contentProviderProviders: ProviderMap<ContentProvider>
  @Multibinds(allowEmpty = true) val serviceProviders: ProviderMap<Service>

  val buildConfig: BuildConfig

  @DependencyGraph.Factory
  fun interface Factory {
    fun create(
      @Provides context: Context,
      @Provides graphHolder: AppGraph.Holder,
    ): AndroidAppGraph
  }
}
