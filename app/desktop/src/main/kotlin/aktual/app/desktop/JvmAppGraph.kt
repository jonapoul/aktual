/**
 * Copyright 2025 Jon Poulton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
