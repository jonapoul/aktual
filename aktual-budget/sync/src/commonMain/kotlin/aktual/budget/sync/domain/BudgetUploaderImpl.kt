package aktual.budget.sync.domain

import aktual.api.client.BudgetSyncApi
import aktual.api.client.UploadRejectedException
import aktual.budget.model.BudgetId
import aktual.budget.model.DbMetadataKey.Companion.BudgetName
import aktual.budget.model.DbMetadataKey.Companion.CloudFileId
import aktual.budget.model.DbMetadataKey.Companion.EncryptKeyId
import aktual.budget.model.DbMetadataKey.Companion.GroupId
import aktual.budget.model.DbMetadataKey.Companion.LastUploaded
import aktual.budget.utils.BudgetExporter
import aktual.budget.utils.BudgetFiles
import aktual.budget.utils.BudgetLocalPreferences
import aktual.budget.utils.BudgetSync
import aktual.budget.utils.BudgetUploader
import aktual.budget.utils.UploadResult
import aktual.core.model.BudgetServer
import aktual.core.utils.Calendar
import aktual.core.utils.UuidGenerator
import aktual.core.utils.runCatchingCancellable
import aktual.di.BudgetScope
import aktual.log
import alakazam.kotlin.CoroutineContexts
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import okio.Path

/** Adapted from upload() in packages/loot-core/src/server/cloud-storage.ts */
@Inject
@ContributesBinding(BudgetScope::class)
class BudgetUploaderImpl
internal constructor(
  private val server: BudgetServer,
  private val exporter: BudgetExporter,
  private val api: BudgetSyncApi,
  private val preferences: BudgetLocalPreferences,
  private val files: BudgetFiles,
  private val sync: BudgetSync,
  private val uuids: UuidGenerator,
  private val calendar: Calendar,
  private val contexts: CoroutineContexts,
) : BudgetUploader {
  override suspend fun upload(): UploadResult {
    if (server !is BudgetServer.Remote) return UploadResult.NoServer

    val metadata = preferences.value
    if (metadata[EncryptKeyId] != null) return UploadResult.Encrypted

    val cloudFileId = metadata[CloudFileId] ?: uuids(::BudgetId)
    var zipPath: Path? = null
    return runCatchingCancellable(
      attempt = {
        val path = exporter.export().path.also { zipPath = it }
        val zip = withContext(contexts.io) { files.fileSystem.read(path) { readByteArray() } }
        val groupId =
          api.uploadBudget(
            id = cloudFileId,
            name = metadata[BudgetName].orEmpty(),
            groupId = metadata[GroupId],
            zip = zip,
          )
        preferences.update { previous ->
          previous
            .set(CloudFileId, cloudFileId)
            .set(GroupId, groupId)
            .set(LastUploaded, calendar.today())
        }
        log.i { "Uploaded as $cloudFileId" }

        // Fills the server's empty message log
        sync.schedule()
        UploadResult.Success
      },
      onFailure = { e ->
        log.w(e) { "Failed uploading as $cloudFileId" }
        if (e is UploadRejectedException) {
          UploadResult.Rejected(e.reason)
        } else {
          UploadResult.Unreachable
        }
      },
      finally = {
        zipPath?.let { path -> withContext(contexts.io) { files.fileSystem.delete(path) } }
      },
    )
  }
}
