/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.app.desktop

import aktual.app.di.JvmViewModelGraph
import aktual.core.di.AppGraph
import aktual.core.model.BuildConfig
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
