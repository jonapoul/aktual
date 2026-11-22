package aktual.budget.list.vm

import aktual.budget.model.BudgetId
import aktual.budget.model.DbMetadataKey.Companion.CloudFileId
import aktual.budget.model.DbMetadataKey.Companion.EncryptKeyId
import aktual.budget.model.DbMetadataKey.Companion.GroupId
import aktual.budget.model.DbMetadataKey.Companion.Id
import aktual.budget.model.DbMetadataKey.Companion.LastUploaded
import aktual.budget.utils.BudgetFiles
import aktual.budget.utils.BudgetImporter
import aktual.budget.utils.ImportResult
import aktual.core.utils.UuidGenerator
import aktual.core.utils.runCatchingCancellable
import aktual.log
import alakazam.kotlin.CoroutineContexts
import alakazam.kotlin.requireMessage
import dev.zacsweers.metro.Inject
import java.io.InputStream
import kotlinx.coroutines.withContext
import okio.buffer

/** Imports a zip in Actual's export format as a new local budget. */
@Inject
internal class ZipBudgetImporter(
  private val importer: BudgetImporter,
  private val files: BudgetFiles,
  private val uuids: UuidGenerator,
  private val contexts: CoroutineContexts,
) {
  suspend fun import(open: () -> InputStream): ZipImport {
    val id = uuids { BudgetId(it) }
    val zip = files.tmp(mkdirs = true) / "$id-import.zip"
    val result =
      runCatchingCancellable(
        attempt = {
          withContext(contexts.io) {
            open().use { input ->
              files.fileSystem.sink(zip).buffer().outputStream().use { input.copyTo(it) }
            }
          }
          importer.import(id, zip) { meta -> meta.set(Id, id.value) - CLEARED_KEYS }
        },
        onFailure = { e ->
          log.e(e) { "Failed reading import file" }
          ImportResult.OtherFailure(e.requireMessage())
        },
        finally = { files.fileSystem.delete(zip, mustExist = false) },
      )
    return ZipImport(id, result)
  }

  private companion object {
    // An imported budget is local-only until it's uploaded
    val CLEARED_KEYS = listOf(CloudFileId, GroupId, LastUploaded, EncryptKeyId)
  }
}

internal data class ZipImport(val id: BudgetId, val result: ImportResult)
