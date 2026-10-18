package aktual.core.logging

import androidx.compose.runtime.Immutable
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format.char
import logcat.LogPriority

@Immutable
data class LogLine(
  val timestamp: LocalDateTime,
  val priority: LogPriority,
  val tag: String?,
  val message: String,
)

object LogFileFormat {
  private const val SEPARATOR = " / "
  private const val PREFIX = "log-"
  private const val SUFFIX = ".log"
  private const val MIN_PARTS = 3
  private const val MAX_PARTS = 4

  // "2025-10-24 16:19:12.175"
  private val TIMESTAMP = LocalDateTime.Format {
    year()
    char('-')
    monthNumber()
    char('-')
    day()
    char(' ')
    hour()
    char(':')
    minute()
    char(':')
    second()
    char('.')
    secondFraction(minLength = 3, maxLength = 3)
  }

  // "log-2025-02-07.log"
  private val FILENAME = LocalDate.Format {
    chars(PREFIX)
    year()
    char('-')
    monthNumber()
    char('-')
    day()
    chars(SUFFIX)
  }

  fun fileName(date: LocalDate): String = FILENAME.format(date)

  fun parseFileName(name: String): LocalDate? = FILENAME.parseOrNull(name)

  fun isLogFile(name: String): Boolean = name.startsWith(PREFIX) && name.endsWith(SUFFIX)

  fun format(time: LocalDateTime, priority: LogPriority, tag: String, message: String): String =
    TIMESTAMP.format(time) + SEPARATOR + priority.char() + SEPARATOR + tag + SEPARATOR + message

  fun format(line: LogLine): String =
    if (line.tag == null) {
      TIMESTAMP.format(line.timestamp) + SEPARATOR + line.priority.char() + SEPARATOR + line.message
    } else {
      format(line.timestamp, line.priority, line.tag, line.message)
    }

  // Lines that don't start with a timestamp belong to the previous line's message, e.g. stack
  // traces
  fun parse(text: String): List<LogLine> {
    val result = mutableListOf<LogLine>()
    for (line in text.lineSequence()) {
      val parsed = parseLine(line)
      if (parsed != null) {
        result += parsed
      } else if (result.isNotEmpty() && line.isNotEmpty()) {
        val last = result.removeAt(result.lastIndex)
        result += last.copy(message = last.message + "\n" + line)
      }
    }
    return result
  }

  private fun parseLine(line: String): LogLine? {
    val parts = line.split(SEPARATOR, limit = MAX_PARTS)
    if (parts.size < MIN_PARTS) return null
    val timestamp = TIMESTAMP.parseOrNull(parts[0]) ?: return null
    val priority = parts[1].singleOrNull()?.toPriority() ?: return null

    // Older files have no tag, so only treat the third part as one if it looks like a tag
    val tag =
      parts[2].takeIf { parts.size == MAX_PARTS && it.isNotEmpty() && it.none(Char::isWhitespace) }
    val message = if (tag == null) parts.drop(2).joinToString(SEPARATOR) else parts[3]
    return LogLine(timestamp, priority, tag, message)
  }

  private fun LogPriority.char(): Char =
    when (this) {
      VERBOSE -> 'V'
      DEBUG -> 'D'
      INFO -> 'I'
      WARN -> 'W'
      ERROR -> 'E'
      ASSERT -> 'A'
    }

  private fun Char.toPriority(): LogPriority? =
    LogPriority.entries.firstOrNull { it.char() == this }
}
