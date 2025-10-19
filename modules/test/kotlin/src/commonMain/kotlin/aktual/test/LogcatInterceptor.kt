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
@file:Suppress("RethrowCaughtException")

package aktual.test

import app.cash.burst.TestFunction
import app.cash.burst.TestInterceptor
import app.cash.burst.coroutines.CoroutineTestFunction
import app.cash.burst.coroutines.CoroutineTestInterceptor
import logcat.LogPriority
import logcat.LogcatLogger

class LogcatInterceptor(
  override val minPriority: LogPriority = LogPriority.VERBOSE,
  override val onlyPrintOnFailure: Boolean = true,
) : ILogcatInterceptor, TestInterceptor {
  override val logMessages = arrayListOf<String>()

  override fun intercept(testFunction: TestFunction) {
    try {
      LogcatLogger.install()
      LogcatLogger.loggers += this
      testFunction()
    } catch (t: Throwable) {
      if (onlyPrintOnFailure) {
        println("---- Captured logcat output ----")
        logMessages.forEach(::println)
        println("---- End captured logcat output ----")
      }
      throw t
    } finally {
      LogcatLogger.uninstall()
      logMessages.clear()
    }
  }
}

class CoLogcatInterceptor(
  override val minPriority: LogPriority = LogPriority.VERBOSE,
  override val onlyPrintOnFailure: Boolean = true,
) : ILogcatInterceptor, CoroutineTestInterceptor {
  override val logMessages = arrayListOf<String>()

  override suspend fun intercept(testFunction: CoroutineTestFunction) = try {
    LogcatLogger.install()
    LogcatLogger.loggers += this
    testFunction()
  } catch (t: Throwable) {
    if (onlyPrintOnFailure) {
      println("---- Captured logcat output ----")
      logMessages.forEach(::println)
      println("---- End captured logcat output ----")
    }
    throw t
  } finally {
    LogcatLogger.uninstall()
    logMessages.clear()
  }
}

interface ILogcatInterceptor : LogcatLogger {
  val minPriority: LogPriority
  val onlyPrintOnFailure: Boolean
  val logMessages: MutableList<String>

  override fun isLoggable(priority: LogPriority, tag: String) = priority >= minPriority

  override fun log(priority: LogPriority, tag: String, message: String) {
    val logMessage = "$priority $tag $message"
    logMessages += logMessage
    if (!onlyPrintOnFailure) println(logMessage)
  }
}
