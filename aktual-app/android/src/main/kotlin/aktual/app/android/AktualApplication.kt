package aktual.app.android

import aktual.core.logging.AktualAndroidLogcatLogger
import aktual.core.logging.CrashReporter
import aktual.core.logging.KermitFileLogger
import aktual.log
import android.app.Application
import android.os.Build.MANUFACTURER
import android.os.Build.MODEL
import android.os.Build.VERSION.RELEASE
import android.os.Build.VERSION.SDK_INT
import android.os.StrictMode
import dev.zacsweers.metro.createGraphFactory
import dev.zacsweers.metrox.android.MetroAppComponentProviders
import dev.zacsweers.metrox.android.MetroApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import logcat.LogPriority
import logcat.LogcatLogger

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

    val logStorage = graph.logStorage
    val minPriority = LogPriority.VERBOSE
    with(LogcatLogger) {
      install()
      loggers += AktualAndroidLogcatLogger(minPriority)
      loggers += KermitFileLogger(logStorage, minPriority)
    }

    val buildConfig = graph.buildConfig
    CrashReporter(
        storage = logStorage,
        version = "${buildConfig.versionName} (${buildConfig.gitHash})",
        platform = "Android $RELEASE (API $SDK_INT), $MANUFACTURER $MODEL",
      )
      .install()

    log.i { "onCreate" }
    log.d { "buildConfig = $buildConfig" }
  }
}
