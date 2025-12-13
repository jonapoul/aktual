package aktual.api.client

import aktual.api.model.internal.AktualHeaders
import aktual.budget.model.BudgetId
import aktual.budget.model.SyncResponse
import aktual.budget.prefs.BudgetLocalPreferences
import aktual.budget.proto.SyncResponseDecoder
import aktual.core.model.LoginToken
import aktual.core.model.Protocol
import aktual.core.model.ServerUrl
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.URLProtocol
import io.ktor.http.path
import io.ktor.utils.io.jvm.javaio.toInputStream
import okio.buffer
import okio.source

interface BudgetSyncApi {
  suspend fun resyncUserFile(
    token: LoginToken,
    budgetId: BudgetId,
  ): SyncResponse
}

class BudgetSyncApiImpl(
  private val serverUrl: ServerUrl,
  private val client: HttpClient,
  private val prefs: BudgetLocalPreferences,
  private val decoder: SyncResponseDecoder,
) : BudgetSyncApi {
  private val urlProtocol = when (serverUrl.protocol) {
    Protocol.Http -> URLProtocol.HTTP
    Protocol.Https -> URLProtocol.HTTPS
  }

  override suspend fun resyncUserFile(
    token: LoginToken,
    budgetId: BudgetId,
  ): SyncResponse {
    val response = client.post {
      url {
        protocol = urlProtocol
        host = serverUrl.baseUrl
        path("/sync")
      }
      header(AktualHeaders.TOKEN, token)
    }

    return decoder(
      source = response
        .bodyAsChannel()
        .toInputStream()
        .source()
        .buffer(),
      metadata = prefs.value,
    )
  }
}
