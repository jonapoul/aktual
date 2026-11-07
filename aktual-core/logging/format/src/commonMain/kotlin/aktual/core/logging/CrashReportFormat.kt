package aktual.core.logging

import aktual.core.model.CrashFile
import androidx.compose.runtime.Immutable
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format.char
import okio.Path

@Immutable
data class CrashReport(
  val file: CrashFile,
  val time: LocalDateTime,
  val version: String,
  val platform: String,
  val thread: String,
  val stackTrace: String,
) {
  // e.g. "java.lang.IllegalStateException: Something broke"
  val summary: String
    get() = stackTrace.lineSequence().firstOrNull().orEmpty()

  // e.g. "IllegalStateException"
  val exceptionName: String
    get() = summary.substringBefore(':').substringAfterLast('.')
}

fun LogStorage.crashDirectory(): Path = directory() / "crashes"

object CrashReportFormat {
  private const val PREFIX = "crash-"
  private const val SUFFIX = ".txt"
  private const val KEY_TIME = "time"
  private const val KEY_VERSION = "version"
  private const val KEY_PLATFORM = "platform"
  private const val KEY_THREAD = "thread"
  private const val KEY_SEPARATOR = ": "

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

  // "crash-20251024-161912-175.txt"
  private val FILENAME = LocalDateTime.Format {
    chars(PREFIX)
    year()
    monthNumber()
    day()
    char('-')
    hour()
    minute()
    second()
    char('-')
    secondFraction(minLength = 3, maxLength = 3)
    chars(SUFFIX)
  }

  fun file(time: LocalDateTime): CrashFile = CrashFile(FILENAME.format(time))

  fun parseFileName(name: String): LocalDateTime? = FILENAME.parseOrNull(name)

  fun isCrashFile(name: String): Boolean = parseFileName(name) != null

  fun format(report: CrashReport): String = buildString {
    appendLine(KEY_TIME + KEY_SEPARATOR + TIMESTAMP.format(report.time))
    appendLine(KEY_VERSION + KEY_SEPARATOR + report.version)
    appendLine(KEY_PLATFORM + KEY_SEPARATOR + report.platform)
    appendLine(KEY_THREAD + KEY_SEPARATOR + report.thread)
    appendLine()
    appendLine(report.stackTrace.trimEnd())
  }

  fun parse(file: CrashFile, text: String): CrashReport? {
    val fileTime = parseFileName(file.name) ?: return null
    val header = text.substringBefore("\n\n")
    val stackTrace = text.substringAfter("\n\n", missingDelimiterValue = "").trimEnd()
    val values =
      header.lineSequence().associate { line ->
        line.substringBefore(KEY_SEPARATOR) to line.substringAfter(KEY_SEPARATOR, "")
      }

    return CrashReport(
      file = file,
      time = values[KEY_TIME]?.let(TIMESTAMP::parseOrNull) ?: fileTime,
      version = values[KEY_VERSION].orEmpty(),
      platform = values[KEY_PLATFORM].orEmpty(),
      thread = values[KEY_THREAD].orEmpty(),
      stackTrace = stackTrace,
    )
  }
}
