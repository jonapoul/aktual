/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.budget.sync.vm

import aktual.api.client.AktualJson
import aktual.api.model.sync.UserFile
import aktual.budget.model.BudgetFiles
import aktual.budget.model.BudgetId
import aktual.budget.model.DbMetadata
import aktual.budget.model.database
import aktual.budget.model.writeMetadata
import alakazam.kotlin.core.CoroutineContexts
import alakazam.kotlin.core.requireMessage
import alakazam.kotlin.time.TimeZoneProvider
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext
import kotlinx.datetime.todayIn
import kotlinx.serialization.SerializationException
import logcat.logcat
import okio.FileSystem
import okio.Path
import okio.buffer
import java.util.zip.ZipException
import java.util.zip.ZipInputStream
import kotlin.time.Clock

/**
 * Adapted from packages/loot-core/src/server/cloud-storage.ts importBuffer()
 */
@Inject
class DatabaseImporter(
  private val contexts: CoroutineContexts,
  private val fileSystem: FileSystem,
  private val budgetFiles: BudgetFiles,
  private val clock: Clock,
  private val timeZones: TimeZoneProvider,
) {
  suspend operator fun invoke(userFile: UserFile, zipPath: Path): ImportResult {
    val directory = budgetFiles.directory(userFile.fileId, mkdirs = true)

    val result = try {
      invokeImpl(userFile, zipPath)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      logcat.e(e) { "Unexpected failure importing $userFile from $zipPath" }
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
      logcat.e(e) { "Failed getting zip entries: dbPath='$nullableDbPath', meta='$nullableMeta'" }
      return ImportResult.NotZipFile
    }

    val metaJson = nullableMeta
    if (metaJson == null || nullableDbPath == null) {
      return ImportResult.InvalidZipFile
    }

    val meta = try {
      AktualJson.decodeFromString(DbMetadata.serializer(), metaJson)
    } catch (e: SerializationException) {
      logcat.e(e) { "Failed deserializing DbMetadata: '$metaJson'" }
      return ImportResult.InvalidMetaFile
    }

    // Update the metadata. The stored file on the server might be out-of-date with a few keys
    val newMeta = meta
      .set(DbMetadata.CloudFileId, userFile.fileId)
      .set(DbMetadata.GroupId, userFile.groupId)
      .set(DbMetadata.LastUploaded, clock.todayIn(timeZones.get()))
      .set(DbMetadata.EncryptKeyId, userFile.encryptMeta?.keyId?.value)

    budgetFiles.writeMetadata(newMeta)
    return ImportResult.Success(newMeta)
  }

  private fun loadIntoDbPath(zip: ZipInputStream, id: BudgetId): Path {
    val dbPath = budgetFiles.database(id, mkdirs = true)
    fileSystem.delete(dbPath, mustExist = false)

    fileSystem
      .sink(dbPath)
      .buffer()
      .outputStream()
      .use { output -> zip.copyTo(output) }

    return dbPath
  }
}
