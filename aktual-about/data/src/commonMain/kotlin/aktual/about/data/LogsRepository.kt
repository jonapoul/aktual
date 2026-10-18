package aktual.about.data

import aktual.core.logging.LogFileFormat
import aktual.core.logging.LogLine
import aktual.core.logging.LogStorage
import aktual.core.model.bytes
import alakazam.kotlin.CoroutineContexts
import alakazam.kotlin.TimeZoneProvider
import dev.zacsweers.metro.Inject
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.datetime.todayIn
import okio.ByteString.Companion.encodeUtf8
import okio.FileSystem
import okio.Path
import okio.buffer

@Inject
class LogsRepository(
  private val storage: LogStorage,
  private val fileSystem: FileSystem,
  private val contexts: CoroutineContexts,
  private val clock: Clock,
  private val timeZones: TimeZoneProvider,
) {
  fun today(): LocalDate = clock.todayIn(timeZones.get())

  suspend fun files(): List<LogFile> =
    withContext(contexts.io) {
      val dir = storage.directory()
      if (!fileSystem.exists(dir)) return@withContext emptyList()

      fileSystem
        .list(dir)
        .mapNotNull { path ->
          val date = LogFileFormat.parseFileName(path.name) ?: return@mapNotNull null
          val size = fileSystem.metadataOrNull(path)?.size ?: 0L
          LogFile(date, size.bytes, errorCount(path))
        }
        .sortedByDescending { it.date }
    }

  // Emits every line in the date's file, then again whenever it grows. Only today's file grows
  fun watch(date: LocalDate): Flow<List<LogLine>> = flow {
    val path = path(date)
    val lines = mutableListOf<LogLine>()
    var offset = 0L
    var first = true

    do {
      val size = fileSystem.metadataOrNull(path)?.size ?: 0L
      if (size < offset) {
        lines.clear()
        offset = 0L
      }
      if (size > offset || first) {
        val (text, read) = readFrom(path, offset)
        offset += read
        lines += LogFileFormat.parse(text)
        emit(lines.toList())
        first = false
      }
      val isToday = date == today()
      if (isToday) delay(POLL_INTERVAL)
    } while (isToday)
  }
    .flowOn(contexts.io)

  suspend fun read(dates: Collection<LocalDate>): String =
    withContext(contexts.io) {
      dates.sortedDescending().joinToString(separator = "\n") { date ->
        val path = path(date)
        val text = if (fileSystem.exists(path)) fileSystem.read(path) { readUtf8() } else ""
        "==== ${path.name} ====\n$text"
      }
    }

  suspend fun deleteAll() =
    withContext(contexts.io) {
      val dir = storage.directory()
      if (!fileSystem.exists(dir)) return@withContext
      fileSystem.list(dir).filter { LogFileFormat.isLogFile(it.name) }.forEach(fileSystem::delete)
    }

  suspend fun delete(date: LocalDate) =
    withContext(contexts.io) { fileSystem.delete(path(date), mustExist = false) }

  private fun path(date: LocalDate): Path = storage.directory() / LogFileFormat.fileName(date)

  // Only reads up to the last full line, in case the logger is part way through writing one
  private fun readFrom(path: Path, offset: Long): Pair<String, Long> {
    if (!fileSystem.exists(path)) return "" to 0L
    val bytes =
      fileSystem.source(path).buffer().use { source ->
        source.skip(offset)
        source.readByteString()
      }
    val end = bytes.lastIndexOf(NEWLINE) + 1
    return bytes.substring(0, end).utf8() to end.toLong()
  }

  private fun errorCount(path: Path): Int =
    fileSystem.read(path) {
      generateSequence { readUtf8Line() }
        .count { line -> ERROR_MARKERS.any { line.startsWith(it, startIndex = TIMESTAMP_LENGTH) } }
    }

  private companion object {
    val POLL_INTERVAL = 1.seconds
    const val TIMESTAMP_LENGTH = 23
    val NEWLINE = "\n".encodeUtf8()
    val ERROR_MARKERS = listOf(" / E / ", " / A / ")
  }
}
