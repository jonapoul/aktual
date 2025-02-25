package actual.android.app

import actual.core.config.BuildConfig
import actual.log.Logger
import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class ActualApplication : Application() {
  @Inject
  lateinit var buildConfig: BuildConfig

  override fun onCreate() {
    super.onCreate()
    Logger.plant(ActualBranch())
    Logger.i("onCreate")
    Logger.d("buildConfig = $buildConfig")
  }
}
