package actual.android.app

import actual.l10n.AndroidLocalization
import actual.l10n.Localization
import actual.logging.ActualAndroidLogcatLogger
import actual.logging.AndroidLogStorage
import actual.logging.LogbackLogger
import alakazam.kotlin.core.BuildConfig
import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import logcat.LogPriority
import logcat.LogcatLogger
import logcat.logcat
import javax.inject.Inject

@HiltAndroidApp
class ActualApplication : Application() {
  @Inject lateinit var buildConfig: BuildConfig

  override fun onCreate() {
    super.onCreate()

    setUpL10n()
    setUpLogging()

    logcat.i { "onCreate" }
    logcat.d { "buildConfig = $buildConfig" }
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
