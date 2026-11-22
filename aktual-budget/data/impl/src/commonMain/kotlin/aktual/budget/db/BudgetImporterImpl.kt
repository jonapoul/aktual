package aktual.budget.db

import aktual.budget.model.BudgetId
import aktual.budget.model.DbMetadata
import aktual.budget.utils.BudgetFiles
import aktual.budget.utils.BudgetImporter
import aktual.budget.utils.ImportResult
import aktual.core.model.AktualJson
import aktual.core.utils.runCatchingCancellable
import aktual.di.AppScope
import aktual.log
import alakazam.kotlin.CoroutineContexts
import alakazam.kotlin.requireMessage
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import java.util.zip.ZipException
import java.util.zip.ZipFile
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import okio.Path
import okio.buffer

/** Adapted from packages/loot-core/src/server/cloud-storage.ts importBuffer() */
@Inject
@ContributesBinding(AppScope::class)
class BudgetImporterImpl
internal constructor(
  private val contexts: CoroutineContexts,
  private val files: BudgetFiles,
) : BudgetImporter {
  private val fileSystem = files.fileSystem

  override suspend fun import(
    id: BudgetId,
    zipPath: Path,
    updateMetadata: (DbMetadata) -> DbMetadata,
  ): ImportResult {
    val result =
      runCatchingCancellable(
        attempt = { withContext(contexts.io) { importImpl(id, zipPath, updateMetadata) } },
        onFailure = { e ->
          log.e(e) { "Unexpected failure importing $id from $zipPath" }
          ImportResult.OtherFailure(e.requireMessage())
        },
      )

    if (result is Failure) {
      fileSystem.deleteRecursively(files.directory(id))
    }

    return result
  }

  private fun importImpl(
    id: BudgetId,
    zipPath: Path,
    updateMetadata: (DbMetadata) -> DbMetadata,
  ): ImportResult {
    val zip =
      try {
        ZipFile(zipPath.toFile())
      } catch (e: ZipException) {
        log.e(e) { "Not a zip file: $zipPath" }
        return NotZipFile
      }

    return zip.use { importFrom(it, id, updateMetadata) }
  }

  private fun importFrom(
    zip: ZipFile,
    id: BudgetId,
    updateMetadata: (DbMetadata) -> DbMetadata,
  ): ImportResult {
    val names = zip.entries().asSequence().map { it.name }.toList()
    val dir = sharedDirectory(names) ?: return InvalidZipFile

    val metaJson =
      zip.getInputStream(zip.getEntry(dir + METADATA_ENTRY)).use { it.reader().readText() }
    val meta =
      try {
        AktualJson.decodeFromString(DbMetadata.serializer(), metaJson)
      } catch (e: SerializationException) {
        log.e(e) { "Failed deserializing DbMetadata: '$metaJson'" }
        return InvalidMetaFile
      }

    val dbPath = files.database(id, mkdirs = true)
    fileSystem.delete(dbPath, mustExist = false)
    zip.getInputStream(zip.getEntry(dir + DB_ENTRY)).use { input ->
      fileSystem.sink(dbPath).buffer().outputStream().use { output -> input.copyTo(output) }
    }

    val newMeta = updateMetadata(meta)
    files.writeMetadata(id, newMeta)
    return ImportResult.Success(newMeta)
  }

  // Both files must come from the same directory: prefer the root, otherwise there must be exactly
  // one directory holding both
  private fun sharedDirectory(names: List<String>): String? {
    val dbDirs =
      names
        .filter { it == DB_ENTRY || it.endsWith("/$DB_ENTRY") }
        .map { it.dropLast(DB_ENTRY.length) }
    val metaDirs =
      names
        .filter { it == METADATA_ENTRY || it.endsWith("/$METADATA_ENTRY") }
        .map { it.dropLast(METADATA_ENTRY.length) }
        .toSet()
    val shared = dbDirs.filter { it in metaDirs }
    return when {
      "" in shared -> ""
      shared.size == 1 -> shared.single()
      else -> null
    }
  }
}
