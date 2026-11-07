package aktual.core.logging

import alakazam.kotlin.TimeZoneProvider
import java.util.concurrent.atomic.AtomicReference
import kotlin.concurrent.thread
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import logcat.logcat
import okio.FileSystem
import okio.Path

// Writes a report file for each uncaught exception, then hands over to the previous handler so the
// app still crashes as normal
class CrashReporter(
  storage: LogStorage,
  private val version: String,
  private val platform: String,
  private val clock: Clock = Clock.System,
  private val timeZone: TimeZoneProvider = Default,
  private val fileSystem: FileSystem = SYSTEM,
) : Thread.UncaughtExceptionHandler {
  private val directory = storage.crashDirectory()
  private var previous: Thread.UncaughtExceptionHandler? = null
  private val lastReported = AtomicReference<Throwable?>(null)

  fun install() {
    previous = Thread.getDefaultUncaughtExceptionHandler()
    Thread.setDefaultUncaughtExceptionHandler(this)
    thread(name = "CrashReporter", isDaemon = true) { prune() }
  }

  override fun uncaughtException(thread: Thread, throwable: Throwable) {
    // Some handlers further down the chain pass the same crash back to the default handler
    if (lastReported.getAndSet(throwable) !== throwable) {
      try {
        logcat.wtf(TAG, throwable) { "Uncaught exception on ${thread.name}" }
        write(thread.name, throwable)
      } catch (_: Exception) {
        // Nothing more we can do
      }
    }
    previous?.uncaughtException(thread, throwable)
  }

  fun write(threadName: String, throwable: Throwable): Path {
    val time = clock.now().toLocalDateTime(timeZone.get())
    val file = CrashReportFormat.file(time)
    val report =
      CrashReport(
        file = file,
        time = time,
        version = version,
        platform = platform,
        thread = threadName,
        stackTrace = throwable.stackTraceToString(),
      )
    fileSystem.createDirectories(directory)
    val path = directory / file.name
    fileSystem.write(path) { writeUtf8(CrashReportFormat.format(report)) }
    return path
  }

  // Drops reports older than MAX_AGE, and the oldest ones past MAX_COUNT
  fun prune() {
    try {
      if (fileSystem.exists(directory)) {
        val cutoff = clock.now() - MAX_AGE
        val zone = timeZone.get()
        fileSystem
          .list(directory)
          .asSequence()
          .mapNotNull { path ->
            CrashReportFormat.parseFileName(path.name)?.let { time -> path to time.toInstant(zone) }
          }
          .sortedByDescending { (_, time) -> time }
          .filterIndexed { index, (_, time) -> index >= MAX_COUNT || time < cutoff }
          .forEach { (path, _) -> fileSystem.delete(path) }
      }
    } catch (e: Exception) {
      logcat.w(TAG, e) { "Failed pruning crash reports" }
    }
  }

  companion object {
    val MAX_AGE = 30.days
    const val MAX_COUNT = 50
    private const val TAG = "CrashReporter"
  }
}
