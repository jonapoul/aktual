package aktual.core.logging

import alakazam.kotlin.TimeZoneProvider
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import co.touchlab.kermit.loggerConfigInit
import co.touchlab.kermit.platformLogWriter
import kotlin.time.Clock
import logcat.LogPriority
import logcat.MinPriorityLogger
import okio.FileSystem

class KermitFileLogger(
  storage: LogStorage,
  override val minPriority: LogPriority,
  clock: Clock = Clock.System,
  timeZone: TimeZoneProvider = TimeZoneProvider.Default,
  fileSystem: FileSystem = FileSystem.SYSTEM,
) : MinPriorityLogger {
  private val fileWriter = FileLogWriter(storage, clock, timeZone, fileSystem)

  private val kermitLogger =
    Logger(config = loggerConfigInit(platformLogWriter(), fileWriter), tag = "Aktual")

  override fun log(priority: LogPriority, tag: String, message: String) =
    kermitLogger.log(priority.severity, tag, throwable = null, message)

  private val LogPriority.severity: Severity
    get() =
      when (this) {
        LogPriority.VERBOSE -> Severity.Verbose
        LogPriority.DEBUG -> Severity.Debug
        LogPriority.INFO -> Severity.Info
        LogPriority.WARN -> Severity.Warn
        LogPriority.ERROR -> Severity.Error
        LogPriority.ASSERT -> Severity.Assert
      }
}
