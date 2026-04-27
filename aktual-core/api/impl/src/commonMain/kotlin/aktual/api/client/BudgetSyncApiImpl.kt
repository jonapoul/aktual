package aktual.api.client

import aktual.budget.model.BudgetScope
import aktual.budget.model.SyncResponse
import aktual.budget.prefs.BudgetLocalPreferences
import aktual.budget.proto.SyncResponseDecoder
import aktual.core.model.Protocol
import aktual.core.model.ServerUrl
import aktual.core.model.Token
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesBinding
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.ContentType
import io.ktor.http.URLProtocol
import io.ktor.http.contentType
import io.ktor.http.path
import io.ktor.utils.io.jvm.javaio.toInputStream
import okio.ByteString
import okio.buffer
import okio.source

@AssistedInject
class BudgetSyncApiImpl(
  @param:AktualClient private val client: HttpClient,
  @Assisted private val serverUrl: ServerUrl,
  private val prefs: BudgetLocalPreferences,
  private val decoder: SyncResponseDecoder,
) : BudgetSyncApi {
  private val urlProtocol =
    when (serverUrl.protocol) {
      Protocol.Http -> URLProtocol.HTTP
      Protocol.Https -> URLProtocol.HTTPS
    }

  override suspend fun syncBudget(token: Token, requestBody: ByteString): SyncResponse {
    val response = client.post {
      url {
        protocol = urlProtocol
        host = serverUrl.baseUrl
        path("/sync/sync")
      }
      header(AktualHeaders.TOKEN, token)
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

  @AssistedFactory
  @ContributesBinding(BudgetScope::class)
  fun interface Factory : BudgetSyncApi.Factory {
    override fun create(@Assisted serverUrl: ServerUrl): BudgetSyncApiImpl
  }
}
