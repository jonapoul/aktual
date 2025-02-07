package actual.android.app

import actual.log.Logger
import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject
import actual.core.config.BuildConfig as ActualBuildConfig

@HiltAndroidApp
class ActualApplication : Application() {
  @Inject
  lateinit var bc: ActualBuildConfig

  @Inject
  lateinit var logger: Logger

  override fun onCreate() {
    super.onCreate()
    Timber.plant(ActualTree())

    logger.i("onCreate")
    logger.d("name=${bc.versionName} code=${bc.versionCode} time=${bc.buildTime}")
    logger.d("manufacturer=${bc.manufacturer} model=${bc.model} os=${bc.os} platform=${bc.platform}")
  }
}
