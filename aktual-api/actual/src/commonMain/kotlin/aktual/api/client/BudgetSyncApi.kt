package aktual.api.client

import aktual.api.model.internal.AktualHeaders
import aktual.budget.model.BudgetId
import aktual.budget.model.BudgetScope
import aktual.budget.model.Message
import aktual.budget.model.SyncResponse
import aktual.budget.model.Timestamp
import aktual.budget.prefs.BudgetLocalPreferences
import aktual.budget.proto.SyncRequestEncoder
import aktual.budget.proto.SyncResponseDecoder
import aktual.core.model.Protocol
import aktual.core.model.ServerUrl
import aktual.core.model.Token
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.URLProtocol
import io.ktor.http.path
import io.ktor.utils.io.jvm.javaio.toInputStream
import okio.buffer
import okio.source

interface BudgetSyncApi {
  suspend fun syncBudget(
    token: Token,
    budgetId: BudgetId,
    groupId: String,
    since: Timestamp,
    messages: List<Message>,
  ): SyncResponse

  fun interface Factory {
    operator fun invoke(
      serverUrl: ServerUrl,
      client: HttpClient,
    ): BudgetSyncApi
  }
}

@Inject
@ContributesBinding(BudgetScope::class)
@SingleIn(BudgetScope::class)
internal class BudgetSyncApiImpl(
  @Assisted private val serverUrl: ServerUrl,
  @Assisted private val client: HttpClient,
  private val prefs: BudgetLocalPreferences,
  private val decoder: SyncResponseDecoder,
  private val encoder: SyncRequestEncoder,
) : BudgetSyncApi {
  private val urlProtocol = when (serverUrl.protocol) {
    Protocol.Http -> URLProtocol.HTTP
    Protocol.Https -> URLProtocol.HTTPS
  }

  override suspend fun syncBudget(
    token: Token,
    budgetId: BudgetId,
    groupId: String,
    since: Timestamp,
    messages: List<Message>,
  ): SyncResponse {
    val encoded = encoder(groupId, budgetId, since, messages)

    val response = client.post {
      url {
        protocol = urlProtocol
        host = serverUrl.baseUrl
        path("/sync")
      }
      header(AktualHeaders.TOKEN, token)
      setBody(encoded.toByteArray())
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

  @AssistedFactory
  @ContributesBinding(BudgetScope::class)
  interface Factory : BudgetSyncApi.Factory {
    override fun invoke(serverUrl: ServerUrl, client: HttpClient): BudgetSyncApiImpl
  }
}
