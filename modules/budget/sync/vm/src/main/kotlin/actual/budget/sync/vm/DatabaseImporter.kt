package actual.budget.sync.vm

import actual.api.client.ActualJson
import actual.api.model.sync.UserFile
import actual.budget.model.BudgetId
import actual.budget.model.DbMetadata
import actual.core.files.BudgetFiles
import actual.core.files.database
import actual.core.files.metadata
import actual.core.model.TimeZones
import alakazam.kotlin.core.CoroutineContexts
import alakazam.kotlin.core.requireMessage
import alakazam.kotlin.logging.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.todayIn
import kotlinx.serialization.SerializationException
import okio.FileSystem
import okio.Path
import okio.buffer
import java.util.zip.ZipException
import java.util.zip.ZipInputStream
import javax.inject.Inject

/**
 * Adapted from packages/loot-core/src/server/cloud-storage.ts importBuffer()
 */
class DatabaseImporter @Inject constructor(
  private val contexts: CoroutineContexts,
  private val fileSystem: FileSystem,
  private val budgetFiles: BudgetFiles,
  private val clock: Clock,
  private val timeZones: TimeZones,
) {
  suspend operator fun invoke(userFile: UserFile, zipPath: Path): ImportResult {
    val directory = budgetFiles.directory(userFile.fileId, mkdirs = true)

    val result = try {
      invokeImpl(userFile, zipPath)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      Logger.e(e, "Unexpected failure importing $userFile from $zipPath")
      ImportResult.OtherFailure(e.requireMessage())
    }

    if (result is ImportResult.Failure) {
      fileSystem.deleteRecursively(directory)
    }

    return result
  }

  private suspend fun invokeImpl(userFile: UserFile, zipPath: Path): ImportResult {
    var nullableMeta: String? = null
    var nullableDbPath: Path? = null

    try {
      withContext(contexts.io) {
        fileSystem.source(zipPath).buffer().use { buffer ->
          ZipInputStream(buffer.inputStream()).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
              if (entry.name == "db.sqlite") nullableDbPath = loadIntoDbPath(zis, userFile.fileId)
              if (entry.name == "metadata.json") nullableMeta = zis.reader().readText()
              entry = zis.nextEntry
            }
          }
        }
      }
    } catch (e: ZipException) {
      Logger.e(e, "Failed getting zip entries: dbPath='%s', meta='%s'", nullableDbPath, nullableMeta)
      return ImportResult.NotZipFile
    }

    val metaJson = nullableMeta
    if (metaJson == null || nullableDbPath == null) {
      return ImportResult.InvalidZipFile
    }

    val meta = try {
      ActualJson.decodeFromString<DbMetadata>(metaJson)
    } catch (e: SerializationException) {
      Logger.e(e, "Failed deserializing DbMetadata: '%s'", metaJson)
      return ImportResult.InvalidMetaFile
    }

    // Update the metadata. The stored file on the server might be out-of-date with a few keys
    val updatedMeta = meta.copy(
      cloudFileId = userFile.fileId,
      groupId = userFile.groupId,
      lastUploaded = clock.todayIn(timeZones.current()),
      encryptKeyId = userFile.encryptMeta?.keyId,
    )

    val metaPath = budgetFiles.metadata(userFile.fileId)
    fileSystem.delete(metaPath, mustExist = false)

    val updatedMetaJson = ActualJson.encodeToString(updatedMeta)
    fileSystem.sink(metaPath).buffer().use { it.writeUtf8(updatedMetaJson) }
    return ImportResult.Success(updatedMeta)
  }

  private fun loadIntoDbPath(zip: ZipInputStream, id: BudgetId): Path {
    val dbPath = budgetFiles.database(id, mkdirs = true)
    fileSystem.delete(dbPath, mustExist = false)
    fileSystem.sink(dbPath)
      .buffer()
      .outputStream()
      .use { output -> zip.copyTo(output) }
    return dbPath
  }
}
