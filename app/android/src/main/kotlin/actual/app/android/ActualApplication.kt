package actual.app.android

import actual.core.model.Password
import actual.core.di.AppGraph
import actual.core.model.ServerUrl
import actual.logging.ActualAndroidLogcatLogger
import actual.logging.AndroidLogStorage
import actual.logging.LogbackLogger
import android.app.Application
import dev.zacsweers.metro.createGraphFactory
import logcat.LogPriority
import logcat.LogcatLogger
import logcat.logcat
import actual.app.android.BuildConfig as ActualBuildConfig

class ActualApplication : Application(), AppGraph.Holder {
  @Suppress("UNNECESSARY_SAFE_CALL", "UnreachableCode")
  private val graph by lazy {
    val factory = createGraphFactory<AndroidAppGraph.Factory>()
    val defaultPassword = ActualBuildConfig.DEFAULT_PASSWORD?.let(::Password) ?: Password.Empty
    val defaultServerUrl = ActualBuildConfig.DEFAULT_URL?.let(::ServerUrl)
    factory.create(
      context = this,
      defaultPassword = { defaultPassword },
      defaultServerUrl = { defaultServerUrl },
      graphHolder = this,
    )
  }

  override fun invoke(): AndroidAppGraph = graph

  override fun onCreate() {
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
