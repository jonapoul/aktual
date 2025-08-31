package actual.app.desktop

import actual.core.di.AppGraph
import dev.zacsweers.metro.createGraphFactory
import logcat.logcat

internal class DesktopAppGraphHolder : AppGraph.Holder {
  private val graph by lazy {
    createGraphFactory<JvmAppGraph.Factory>()
      .create(graphHolder = this)
      .also { graph ->
        logcat.i { "App started" }
        logcat.d { "buildConfig = ${graph.buildConfig}" }
      }
  }

  override fun get(): AppGraph = graph
}
