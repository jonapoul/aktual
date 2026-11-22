package aktual.budget.db

import aktual.budget.model.BudgetId
import aktual.budget.model.DbMetadata
import aktual.budget.model.DbMetadataKey.Companion.BudgetName
import aktual.budget.model.DbMetadataKey.Companion.ResetClock
import aktual.budget.utils.BudgetExport
import aktual.budget.utils.BudgetExporter
import aktual.budget.utils.BudgetFiles
import aktual.budget.utils.BudgetLocalPreferences
import aktual.core.utils.Calendar
import aktual.di.BudgetScope
import alakazam.kotlin.CoroutineContexts
import app.cash.sqldelight.db.SqlDriver
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okio.Path
import okio.buffer

/** Adapted from packages/loot-core/src/server/cloud-storage.ts exportBuffer() */
@Inject
@ContributesBinding(BudgetScope::class)
class BudgetExporterImpl
internal constructor(
  private val id: BudgetId,
  private val driver: SqlDriver,
  private val files: BudgetFiles,
  private val preferences: BudgetLocalPreferences,
  private val calendar: Calendar,
  private val contexts: CoroutineContexts,
) : BudgetExporter {
  private val fileSystem = files.fileSystem

  override suspend fun export(): BudgetExport =
    withContext(contexts.io) {
      val snapshot = files.exportDatabase(id, mkdirs = true)
      val zip = files.exportZip(id, mkdirs = true)
      fileSystem.delete(snapshot, mustExist = false)
      fileSystem.delete(zip, mustExist = false)

      // A new client opening the file gets a unique node
      val metadata = preferences.value.set(ResetClock, true)

      try {
        snapshotDatabase(snapshot)
        writeZip(zip, snapshot, metadata)
      } finally {
        fileSystem.delete(snapshot, mustExist = false)
      }

      BudgetExport(path = zip, fileName = fileName(metadata))
    }

  // VACUUM INTO gives a consistent copy without closing the open database. Aktual never writes the
  // kvcache tables, and Actual clears them when importing a file, so the copy isn't cleaned up
  private suspend fun snapshotDatabase(snapshot: Path) {
    driver.execute(null, "VACUUM INTO ?", 1) { bindString(0, snapshot.toString()) }.await()
  }

  private fun writeZip(zip: Path, snapshot: Path, metadata: DbMetadata) {
    val json = Json.encodeToString(DbMetadata.serializer(), metadata)
    fileSystem.sink(zip).buffer().outputStream().use { output ->
      ZipOutputStream(output).use { zos ->
        zos.putNextEntry(ZipEntry(DB_ENTRY))
        fileSystem.source(snapshot).buffer().inputStream().use { input -> input.copyTo(zos) }
        zos.closeEntry()

        zos.putNextEntry(ZipEntry(METADATA_ENTRY))
        zos.write(json.encodeToByteArray())
        zos.closeEntry()
      }
    }
  }

  private fun fileName(metadata: DbMetadata): String {
    val name = metadata[BudgetName].orEmpty().replace(INVALID_FILENAME_CHARS, "-")
    return "${calendar.today()}-$name.zip"
  }

  internal companion object {
    const val DB_ENTRY = "db.sqlite"
    const val METADATA_ENTRY = "metadata.json"
    private val INVALID_FILENAME_CHARS = Regex("""[\\/:*?"<>|]""")
  }
}
