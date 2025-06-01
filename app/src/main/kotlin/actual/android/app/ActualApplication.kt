package actual.android.app

import actual.core.files.BudgetFiles
import actual.logging.ActualLogging
import actual.logging.AndroidLogStorage
import actual.logging.AndroidTreeFactory
import alakazam.kotlin.core.BuildConfig
import alakazam.kotlin.logging.Logger
import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class ActualApplication : Application() {
  @Inject lateinit var buildConfig: BuildConfig
  @Inject lateinit var budgetFiles: BudgetFiles

  override fun onCreate() {
    super.onCreate()
    ActualLogging.init(AndroidTreeFactory, AndroidLogStorage(this))
    Logger.i("onCreate")
    Logger.d("buildConfig = $buildConfig")

    // Create temporary folder, then make sure it gets deleted when the app closes
    budgetFiles
      .tmp(mkdirs = true)
      .toFile()
      .deleteOnExit()
  }
}
