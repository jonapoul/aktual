package aktual.core.logging

import alakazam.kotlin.TimeZoneProvider
import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import okio.FileSystem
import okio.buffer
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

internal class FileLogWriter(
  storage: LogStorage,
  private val clock: Clock,
  private val timeZone: TimeZoneProvider,
  private val fileSystem: FileSystem,
) : LogWriter() {
  private val logDirectory = storage.directory()

  override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
    try {
      val time = clock.now()
      val localTime = time.toLocalDateTime(timeZone.get())

      val timestamp = LOG_FORMAT.format(localTime)
      val logLine = "$timestamp / ${severity.char()} / $message\n"

      val logFile = logDirectory / FILENAME_FORMAT.format(localTime)
      fileSystem
        .appendingSink(logFile)
        .buffer()
        .use { sink -> sink.writeUtf8(logLine) }

      @Suppress("MagicNumber")
      if (time.epochSeconds % 100 == 0L) {
        cleanOldLogs()
      }
    } catch (_: Exception) {
      // Silently fail
    }
  }

  private fun cleanOldLogs() = try {
    val now = clock.now()
    val cutoffTime = (now - MAX_HISTORY).toEpochMilliseconds()

    fileSystem
      .list(logDirectory)
      .asSequence()
      .filter { it.name.startsWith("log-") && it.name.endsWith(".log") }
      .forEach { file ->
        val modified = fileSystem.metadataOrNull(file)?.lastModifiedAtMillis
        if (modified != null && modified < cutoffTime) {
          fileSystem.delete(file)
        }
      }
  } catch (_: Exception) {
    // Silently fail
  }

  private companion object {
    val MAX_HISTORY = 30.days

    // "2025-10-24 16:19:12.175"
    val LOG_FORMAT = LocalDateTime.Format {
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
    val FILENAME_FORMAT = LocalDateTime.Format {
      chars("log-")
      year()
      char('-')
      monthNumber()
      char('-')
      day()
      chars(".log")
    }

    private fun Severity.char(): Char = when (this) {
      Severity.Verbose -> 'V'
      Severity.Debug -> 'D'
      Severity.Info -> 'I'
      Severity.Warn -> 'W'
      Severity.Error -> 'E'
      Severity.Assert -> 'A'
    }
  }
}
