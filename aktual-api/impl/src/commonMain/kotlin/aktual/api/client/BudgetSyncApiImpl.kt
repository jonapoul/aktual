package aktual.api.client

import aktual.api.model.account.FailureReason
import aktual.api.model.sync.UpdateUserFileNameRequest
import aktual.api.model.sync.UploadUserFileResponse
import aktual.budget.model.BudgetId
import aktual.budget.model.SyncResponse
import aktual.budget.proto.SyncResponseDecoder
import aktual.budget.utils.BudgetLocalPreferences
import aktual.core.model.BudgetServer
import aktual.di.BudgetScope
import dev.zacsweers.metro.ContributesBinding
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.encodeURLParameter
import io.ktor.utils.io.jvm.javaio.toInputStream
import okio.ByteString
import okio.buffer
import okio.source

@ContributesBinding(BudgetScope::class)
class BudgetSyncApiImpl
internal constructor(
  @param:AktualClient private val client: HttpClient,
  private val server: BudgetServer,
  private val prefs: BudgetLocalPreferences,
  private val decoder: SyncResponseDecoder,
) : BudgetSyncApi {
  override suspend fun syncBudget(requestBody: ByteString): SyncResponse {
    val remote = remote()
    val response =
      try {
        client.post {
          url(remote.url, "/sync/sync")
          header(AktualHeaders.TOKEN, remote.token.value)
          setBody(requestBody.toByteArray())

          // Required by the Actual sync server's express.raw() middleware.
          // See packages/sync-server/src/app-sync.ts
          contentType(ContentType("application", "actual-sync"))
        }
      } catch (e: ClientRequestException) {
        if (e.response.status == BadRequest) {
          throw SyncRejectedException(FailureReason(e.response.bodyAsText()), e)
        }
        throw e
      }

    return decoder(
      source = response.bodyAsChannel().toInputStream().source().buffer(),
      metadata = prefs.value,
    )
  }

  override suspend fun renameBudget(id: BudgetId, name: String) {
    val remote = remote()
    client.post {
      url(remote.url, "/sync/update-user-filename")
      contentType(ContentType.Application.Json)
      setBody(UpdateUserFileNameRequest(id, name, remote.token))
    }
  }

  override suspend fun uploadBudget(
    id: BudgetId,
    name: String,
    groupId: String?,
    zip: ByteArray,
  ): String {
    val remote = remote()
    val response =
      try {
        client.post {
          url(remote.url, "/sync/upload-user-file")
          header(AktualHeaders.TOKEN, remote.token.value)
          header(AktualHeaders.FILE_ID, id)
          header(AktualHeaders.NAME, name.encodeURLParameter())
          header(AktualHeaders.FORMAT, SYNC_FORMAT)
          groupId?.let { header(AktualHeaders.GROUP_ID, it) }
          contentType(ContentType("application", "encrypted-file"))
          setBody(zip)
        }
      } catch (e: ClientRequestException) {
        throw UploadRejectedException(FailureReason(e.response.bodyAsText()), e)
      }
    return response.body<UploadUserFileResponse>().groupId
  }

  private fun remote(): BudgetServer.Remote =
    checkNotNull(server as? Remote) { "No server to sync with" }

  private companion object {
    const val SYNC_FORMAT = "2"
  }
}
