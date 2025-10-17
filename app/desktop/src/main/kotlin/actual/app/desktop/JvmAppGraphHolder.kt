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
package actual.app.desktop

import actual.core.di.AppGraph
import dev.zacsweers.metro.createGraphFactory
import logcat.logcat

internal class JvmAppGraphHolder : AppGraph.Holder {
  private val graph by lazy {
    createGraphFactory<JvmAppGraph.Factory>()
      .create(graphHolder = this)
      .also { graph ->
        logcat.i { "App started" }
        logcat.d { "buildConfig = ${graph.buildConfig}" }
      }
  }

  override fun get(): JvmAppGraph = graph
}
