/**
 * Copyright 2025 Jon Poulton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
