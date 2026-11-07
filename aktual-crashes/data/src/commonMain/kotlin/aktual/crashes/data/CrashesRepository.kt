package aktual.crashes.data

import aktual.core.logging.CrashReport
import aktual.core.logging.CrashReportFormat
import aktual.core.logging.LogStorage
import aktual.core.logging.crashDirectory
import aktual.core.model.CrashFile
import alakazam.kotlin.CoroutineContexts
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.withContext
import okio.FileSystem
import okio.Path

@Inject
class CrashesRepository
internal constructor(
  private val storage: LogStorage,
  private val fileSystem: FileSystem,
  private val contexts: CoroutineContexts,
) {
  suspend fun reports(): List<CrashReport> =
    withContext(contexts.io) {
      val dir = storage.crashDirectory()
      if (!fileSystem.exists(dir)) return@withContext emptyList()
      fileSystem
        .list(dir)
        .asSequence()
        .filter { CrashReportFormat.isCrashFile(it.name) }
        .mapNotNull { path -> readReport(path) }
        .sortedByDescending { it.time }
        .toList()
    }

  suspend fun report(file: CrashFile): CrashReport? =
    withContext(contexts.io) { readReport(path(file)) }

  suspend fun read(file: CrashFile): String? =
    withContext(contexts.io) {
      val path = path(file)
      if (fileSystem.exists(path)) fileSystem.read(path) { readUtf8() } else null
    }

  suspend fun delete(file: CrashFile) =
    withContext(contexts.io) { fileSystem.delete(path(file), mustExist = false) }

  suspend fun deleteAll() =
    withContext(contexts.io) {
      val dir = storage.crashDirectory()
      if (!fileSystem.exists(dir)) return@withContext
      fileSystem
        .list(dir)
        .filter { CrashReportFormat.isCrashFile(it.name) }
        .forEach(fileSystem::delete)
    }

  private fun path(file: CrashFile): Path {
    require(CrashReportFormat.isCrashFile(file.name)) { "Not a crash report: $file" }
    return storage.crashDirectory() / file.name
  }

  private fun readReport(path: Path): CrashReport? {
    if (!fileSystem.exists(path)) return null
    val text = fileSystem.read(path) { readUtf8() }
    return CrashReportFormat.parse(CrashFile(path.name), text)
  }
}
