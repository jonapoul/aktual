package actual.test

import app.cash.burst.TestFunction
import app.cash.burst.TestInterceptor
import app.cash.burst.coroutines.CoroutineTestFunction
import app.cash.burst.coroutines.CoroutineTestInterceptor
import logcat.LogPriority
import logcat.LogcatLogger

class LogcatInterceptor(
  private val minPriority: LogPriority = LogPriority.VERBOSE,
  private val onlyPrintOnFailure: Boolean = true,
) : TestInterceptor, CoroutineTestInterceptor, LogcatLogger {
  private val logMessages = arrayListOf<String>()

  override suspend fun intercept(testFunction: CoroutineTestFunction) = runTest { testFunction() }

  override fun intercept(testFunction: TestFunction) = runTest { testFunction() }

  @Suppress("RethrowCaughtException")
  private inline fun runTest(testFunction: () -> Unit) = try {
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

  override fun isLoggable(priority: LogPriority, tag: String) = priority >= minPriority

  override fun log(priority: LogPriority, tag: String, message: String) {
    val logMessage = "$priority $tag $message"
    logMessages += logMessage
    if (!onlyPrintOnFailure) println(logMessage)
  }
}
