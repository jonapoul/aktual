/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.app.desktop

import aktual.core.di.AppGraph
import aktual.core.model.BuildConfig
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import kotlin.time.Clock

@DependencyGraph(AppScope::class)
interface JvmAppGraph : AppGraph {
  val buildConfig: BuildConfig
  val windowPreferences: WindowStatePreferences
  val clock: Clock
}
