package actual.app.android

import actual.account.model.Password
import actual.app.android.AndroidAppGraph
import actual.core.di.AppGraph
import actual.core.model.ServerUrl
import actual.logging.ActualAndroidLogcatLogger
import actual.logging.AndroidLogStorage
import actual.logging.LogbackLogger
import alakazam.kotlin.core.BasicBuildConfig
import android.app.Application
import android.content.Context
import android.os.Build
import dev.zacsweers.metro.createGraphFactory
import logcat.LogPriority
import logcat.LogcatLogger
import logcat.logcat
import actual.app.android.BuildConfig as ActualBuildConfig
import alakazam.kotlin.core.BuildConfig as AlakazamBuildConfig

class ActualApplication : Application(), AppGraph.Holder {
  @Suppress("UNNECESSARY_SAFE_CALL", "UnreachableCode")
  private val graph by lazy {
    val factory = createGraphFactory<AndroidAppGraph.Factory>()
    val defaultPassword = ActualBuildConfig.DEFAULT_PASSWORD?.let(::Password) ?: Password.Empty
    val defaultServerUrl = ActualBuildConfig.DEFAULT_URL?.let(::ServerUrl)
    factory.create(
      context = this,
      buildConfig = buildConfig(context = this),
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

  internal fun buildConfig(context: Context): AlakazamBuildConfig = BasicBuildConfig(
    debug = ActualBuildConfig.DEBUG,
    applicationId = ActualBuildConfig.APPLICATION_ID,
    versionCode = ActualBuildConfig.VERSION_CODE,
    versionName = ActualBuildConfig.VERSION_NAME,
    buildTime = ActualBuildConfig.BUILD_TIME,
    gitId = ActualBuildConfig.GIT_HASH,
    manufacturer = Build.MANUFACTURER,
    model = Build.MODEL,
    os = Build.VERSION.SDK_INT,
    platform = context.getString(R.string.app_name),
    repoName = REPO_NAME,
    repoUrl = "https://github.com/${REPO_NAME}",
  )

  private companion object {
    const val REPO_NAME = "jonapoul/actual-android"
  }
}
