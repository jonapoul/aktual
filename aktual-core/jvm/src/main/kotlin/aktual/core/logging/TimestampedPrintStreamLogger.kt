package aktual.core.logging

import java.io.PrintStream
import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.UtcOffset
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.char
import logcat.LogPriority

class TimestampedPrintStreamLogger(
  private val stream: PrintStream,
  private val clock: Clock,
  override val minPriority: LogPriority,
) : MinPriorityLogger {
  override fun log(priority: LogPriority, tag: String, message: String) {
    val time = clock.now().format(TIMESTAMP_FORMAT)
    stream.println("$time ${priority.char()}/$tag: $message")
  }

  private fun LogPriority.char() =
    when (this) {
      VERBOSE -> 'V'
      DEBUG -> 'D'
      INFO -> 'I'
      WARN -> 'W'
      ERROR -> 'E'
      ASSERT -> 'A'
    }

  private companion object {
    val TIMESTAMP_FORMAT = DateTimeComponents.Format {
      date(LocalDate.Formats.ISO)
      char('T')
      hour()
      char(':')
      minute()
      char(':')
      second()
      char('.')
      secondFraction(3)
      offset(UtcOffset.Formats.ISO)
    }
  }
}
