package logcat

import assertk.assertThat
import assertk.assertions.isEqualTo

internal class TestLogcatLogger : LogcatLogger {
  var latestLog: Log? = null
  var latestPriority: LogPriority? = null
  var shouldLog = true

  data class Log(val priority: LogPriority, val tag: String, val message: String)

  override fun isLoggable(priority: LogPriority, tag: String): Boolean {
    latestPriority = priority
    return shouldLog
  }

  override fun log(priority: LogPriority, tag: String, message: String) {
    latestLog = Log(priority, tag, message)
  }

  fun assertLatest(priority: LogPriority, tag: String, message: String) =
    assertThat(latestLog).isEqualTo(Log(priority, tag, message))
}
