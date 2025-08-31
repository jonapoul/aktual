package actual.app.android

import actual.core.di.AppGraph
import actual.logging.ActualAndroidLogcatLogger
import actual.logging.AndroidLogStorage
import actual.logging.LogbackLogger
import android.app.Application
import android.os.StrictMode
import dev.zacsweers.metro.createGraphFactory
import logcat.LogPriority
import logcat.LogcatLogger
import logcat.logcat

class ActualApplication : Application(), AppGraph.Holder {
  private val graph by lazy {
    createGraphFactory<AndroidAppGraph.Factory>().create(
      context = this,
      graphHolder = this,
    )
  }

  override fun invoke(): AndroidAppGraph = graph

  override fun onCreate() {
    if (BuildConfig.DEBUG) {
      StrictMode.setThreadPolicy(threadPolicy())
      StrictMode.setVmPolicy(vmPolicy())
    }

    super.onCreate()

    val logStorage = AndroidLogStorage(context = this)
    val minPriority = LogPriority.VERBOSE
    with(LogcatLogger) {
      install()
      loggers += ActualAndroidLogcatLogger(minPriority)
      loggers += LogbackLogger(logStorage, minPriority)
    }

    logcat.i { "onCreate" }
    logcat.d { "buildConfig = ${graph.buildConfig}" }
  }
}
