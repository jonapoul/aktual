package aktual.core.logging

import logcat.LogPriority
import logcat.LogcatLogger

interface MinPriorityLogger : LogcatLogger {
  val minPriority: LogPriority

  override fun isLoggable(priority: LogPriority, tag: String) = priority >= minPriority
}
