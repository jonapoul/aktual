/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.app.desktop

import aktual.core.di.AppGraph
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
