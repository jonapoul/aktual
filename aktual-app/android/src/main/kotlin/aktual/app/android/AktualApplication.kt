package aktual.app.android

import aktual.core.logging.AktualAndroidLogcatLogger
import aktual.core.logging.AndroidLogStorage
import aktual.core.logging.KermitFileLogger
import android.app.Application
import android.os.StrictMode
import dev.zacsweers.metro.createGraphFactory
import dev.zacsweers.metrox.android.MetroAppComponentProviders
import dev.zacsweers.metrox.android.MetroApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import logcat.LogPriority
import logcat.LogcatLogger
import logcat.logcat

class AktualApplication : Application(), MetroApplication {
  private val graph by lazy { createGraphFactory<AndroidAppGraph.Factory>().create(context = this) }
  override val appComponentProviders: MetroAppComponentProviders
    get() = graph

  override fun onCreate() {
    if (BuildConfig.DEBUG) {
      StrictMode.setThreadPolicy(threadPolicy())
      StrictMode.setVmPolicy(vmPolicy())
    }

    @Suppress("InjectDispatcher")
    CoroutineScope(Dispatchers.Default).launch { graph.initialiser(graph) }

    super.onCreate()

    val logStorage = AndroidLogStorage(context = this)
    val minPriority = LogPriority.VERBOSE
    with(LogcatLogger) {
      install()
      loggers += AktualAndroidLogcatLogger(minPriority)
      loggers += KermitFileLogger(logStorage, minPriority)
    }

    logcat.i { "onCreate" }
    logcat.d { "buildConfig = ${graph.buildConfig}" }
  }
}
