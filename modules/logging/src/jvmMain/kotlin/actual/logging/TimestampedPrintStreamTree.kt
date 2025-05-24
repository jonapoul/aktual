package actual.logging

import alakazam.kotlin.logging.BasicTree
import alakazam.kotlin.logging.LogLevel
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.UtcOffset
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.char
import java.io.PrintStream

internal class TimestampedPrintStreamTree(
  private val stream: PrintStream,
  private val clock: Clock,
) : BasicTree() {
  override fun log(level: LogLevel, tag: String?, message: String, t: Throwable?) {
    val time = clock.now().format(TIMESTAMP_FORMAT)
    stream.println("$time ${level.char()}/$tag: $message")
    t?.printStackTrace(stream)
  }

  private fun LogLevel.char() = when (this) {
    LogLevel.Verbose -> 'V'
    LogLevel.Debug -> 'D'
    LogLevel.Info -> 'I'
    LogLevel.Warn -> 'W'
    LogLevel.Error -> 'E'
    LogLevel.Assert -> 'A'
  }

  private companion object {
    val TIMESTAMP_FORMAT = DateTimeComponents.Companion.Format {
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
