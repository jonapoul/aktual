package logcat

interface MinPriorityLogger : LogcatLogger {
  val minPriority: LogPriority

  override fun isLoggable(priority: LogPriority, tag: String) = priority >= minPriority
}
