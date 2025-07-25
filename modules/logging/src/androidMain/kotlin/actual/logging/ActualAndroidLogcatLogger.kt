package actual.logging

import logcat.AndroidLogcatLogger
import logcat.LogPriority
import logcat.MinPriorityLogger

class ActualAndroidLogcatLogger(override val minPriority: LogPriority) : MinPriorityLogger {
  private val delegate = AndroidLogcatLogger(minPriority)

  override fun log(priority: LogPriority, tag: String, message: String) {
    delegate.log(priority, tag, message = "ACTUAL: $message")
  }
}
