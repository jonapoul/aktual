/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.app.android

import aktual.core.di.AppGraph
import aktual.logging.AktualAndroidLogcatLogger
import aktual.logging.AndroidLogStorage
import aktual.logging.LogbackLogger
import android.app.Application
import android.os.StrictMode
import dev.zacsweers.metro.createGraphFactory
import logcat.LogPriority
import logcat.LogcatLogger
import logcat.logcat

class AktualApplication : Application(), AppGraph.Holder {
  private val graph by lazy {
    createGraphFactory<AndroidAppGraph.Factory>().create(
      context = this,
      graphHolder = this,
    )
  }

  override fun get(): AndroidAppGraph = graph

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
      loggers += AktualAndroidLogcatLogger(minPriority)
      loggers += LogbackLogger(logStorage, minPriority)
    }

    logcat.i { "onCreate" }
    logcat.d { "buildConfig = ${graph.buildConfig}" }
  }
}
