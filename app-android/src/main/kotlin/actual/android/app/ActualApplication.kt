package actual.android.app

import actual.app.di.AndroidAppGraph
import actual.l10n.AndroidLocalization
import actual.l10n.Localization
import actual.logging.ActualAndroidLogcatLogger
import actual.logging.AndroidLogStorage
import actual.logging.LogbackLogger
import android.app.Application
import logcat.LogPriority
import logcat.LogcatLogger
import logcat.logcat

class ActualApplication : Application(), AndroidAppGraph.Holder {
  private var nullableGraph: AndroidAppGraph? = null

  override fun graph() = requireNotNull(nullableGraph) {
    "AndroidAppGraph hasn't been initialised yet"
  }

  override fun onCreate() {
    super.onCreate()
    val buildConfig = buildConfig(context = this)

    nullableGraph = setUpDi(buildConfig, context = this)
    setUpL10n()
    setUpLogging()

    logcat.i { "onCreate" }
    logcat.d { "buildConfig = $buildConfig" }
    logcat.d { "graph = $nullableGraph" }
  }

  private fun setUpL10n() {
    val l10n = AndroidLocalization(context = this)
    Localization.set(l10n)
  }

  private fun setUpLogging() {
    val logStorage = AndroidLogStorage(context = this)
    val minPriority = LogPriority.VERBOSE
    with(LogcatLogger) {
      install()
      loggers += ActualAndroidLogcatLogger(minPriority)
      loggers += LogbackLogger(logStorage, minPriority)
    }
  }
}
