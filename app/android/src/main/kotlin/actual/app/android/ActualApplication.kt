package actual.app.android

import actual.account.model.Password
import actual.core.model.ServerUrl
import actual.logging.ActualAndroidLogcatLogger
import actual.logging.AndroidLogStorage
import actual.logging.LogbackLogger
import alakazam.kotlin.core.BuildConfig
import android.app.Application
import android.content.Context
import dev.zacsweers.metro.createGraphFactory
import logcat.LogPriority
import logcat.LogcatLogger
import logcat.logcat
import actual.app.android.BuildConfig as ActualBuildConfig

class ActualApplication : Application(), AndroidAppGraph.Holder {
  private var nullableGraph: AndroidAppGraph? = null

  override fun graph() = requireNotNull(nullableGraph) {
    "AndroidAppGraph hasn't been initialised yet"
  }

  override fun onCreate() {
    super.onCreate()
    val buildConfig = buildConfig(context = this)

    nullableGraph = setUpDi(buildConfig, context = this)
    setUpLogging()

    logcat.i { "onCreate" }
    logcat.d { "buildConfig = $buildConfig" }
    logcat.d { "graph = $nullableGraph" }
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

  @Suppress("UNNECESSARY_SAFE_CALL")
  private fun setUpDi(
    buildConfig: BuildConfig,
    context: Context,
  ): AndroidAppGraph {
    val factory = createGraphFactory<AndroidAppGraph.Factory>()
    val defaultPassword = ActualBuildConfig.DEFAULT_PASSWORD?.let(::Password) ?: Password.Empty
    val defaultServerUrl = ActualBuildConfig.DEFAULT_URL?.let(::ServerUrl)
    return factory.create(
      context = context,
      buildConfig = buildConfig,
      defaultPassword = { defaultPassword },
      defaultServerUrl = { defaultServerUrl },
    )
  }
}
