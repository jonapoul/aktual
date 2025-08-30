package actual.test

import app.cash.burst.TestFunction
import app.cash.burst.TestInterceptor
import logcat.LogPriority
import logcat.LogcatLogger

class LogcatInterceptor(
  private val minPriority: LogPriority = LogPriority.VERBOSE,
  private val onlyPrintOnFailure: Boolean = true,
) : TestInterceptor, LogcatLogger {
  private val logMessages = arrayListOf<String>()

  @Suppress("RethrowCaughtException")
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

  override fun isLoggable(priority: LogPriority, tag: String) = priority >= minPriority

  override fun log(priority: LogPriority, tag: String, message: String) {
    val logMessage = "$priority $tag $message"
    logMessages += logMessage
    if (!onlyPrintOnFailure) println(logMessage)
  }
}
