package aktual.core.logging

import alakazam.kotlin.TimeZoneProvider
import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlinx.datetime.toLocalDateTime
import logcat.LogPriority
import okio.FileSystem
import okio.buffer

internal class FileLogWriter(
  storage: LogStorage,
  private val clock: Clock,
  private val timeZone: TimeZoneProvider,
  private val fileSystem: FileSystem,
) : LogWriter() {
  private val logDirectory = storage.directory()
  private var hasDirectory = false

  override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
    try {
      val time = clock.now()
      val localTime = time.toLocalDateTime(timeZone.get())

      val logLine = LogFileFormat.format(localTime, severity.priority(), tag, message) + "\n"

      ensureDirectory()
      val logFile = logDirectory / LogFileFormat.fileName(localTime.date)
      fileSystem.appendingSink(logFile).buffer().use { sink -> sink.writeUtf8(logLine) }

      @Suppress("MagicNumber")
      if (time.epochSeconds % 100 == 0L) {
        cleanOldLogs()
      }
    } catch (_: Exception) {
      // Silently fail
    }
  }

  private fun ensureDirectory() {
    if (hasDirectory) return
    fileSystem.createDirectories(logDirectory)
    hasDirectory = true
  }

  private fun cleanOldLogs() =
    try {
      val now = clock.now()
      val cutoffTime = (now - MAX_HISTORY).toEpochMilliseconds()

      fileSystem
        .list(logDirectory)
        .asSequence()
        .filter { LogFileFormat.isLogFile(it.name) }
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

    private fun Severity.priority(): LogPriority =
      when (this) {
        Verbose -> VERBOSE
        Debug -> DEBUG
        Info -> INFO
        Warn -> WARN
        Severity.Error -> ERROR
        Assert -> ASSERT
      }
  }
}
