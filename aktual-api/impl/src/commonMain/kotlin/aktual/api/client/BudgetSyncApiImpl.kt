package aktual.api.client

import aktual.api.model.sync.UpdateUserFileNameRequest
import aktual.budget.BudgetLocalPreferences
import aktual.budget.model.BudgetId
import aktual.budget.model.SyncResponse
import aktual.budget.proto.SyncResponseDecoder
import aktual.core.model.BudgetServer
import aktual.di.BudgetScope
import dev.zacsweers.metro.ContributesBinding
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.path
import io.ktor.utils.io.jvm.javaio.toInputStream
import okio.ByteString
import okio.buffer
import okio.source

@ContributesBinding(BudgetScope::class)
class BudgetSyncApiImpl(
  @param:AktualClient private val client: HttpClient,
  private val server: BudgetServer,
  private val prefs: BudgetLocalPreferences,
  private val decoder: SyncResponseDecoder,
) : BudgetSyncApi {
  override suspend fun syncBudget(requestBody: ByteString): SyncResponse {
    val remote = remote()
    val response = client.post {
      url {
        protocol = remote.url.protocol()
        host = remote.url.baseUrl
        path("/sync/sync")
      }
      header(AktualHeaders.TOKEN, remote.token)
      setBody(requestBody.toByteArray())

      // Required by the Actual sync server's express.raw() middleware.
      // See packages/sync-server/src/app-sync.ts
      contentType(ContentType("application", "actual-sync"))
    }

    return decoder(
      source = response.bodyAsChannel().toInputStream().source().buffer(),
      metadata = prefs.value,
    )
  }

  override suspend fun renameBudget(id: BudgetId, name: String) {
    val remote = remote()
    client.post {
      url {
        protocol = remote.url.protocol()
        host = remote.url.baseUrl
        path("/sync/update-user-filename")
      }
      contentType(ContentType.Application.Json)
      setBody(UpdateUserFileNameRequest(id, name, remote.token))
    }
  }

  private fun remote(): BudgetServer.Remote =
    checkNotNull(server as? BudgetServer.Remote) { "No server to sync with" }
}
