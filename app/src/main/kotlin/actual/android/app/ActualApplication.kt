package actual.android.app

import alakazam.kotlin.core.BuildConfig
import alakazam.kotlin.logging.Logger
import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class ActualApplication : Application() {
  @Inject
  lateinit var buildConfig: BuildConfig

  override fun onCreate() {
    super.onCreate()
    Logger.plant(ActualTree())
    Logger.i("onCreate")
    Logger.d("buildConfig = $buildConfig")
  }
}
