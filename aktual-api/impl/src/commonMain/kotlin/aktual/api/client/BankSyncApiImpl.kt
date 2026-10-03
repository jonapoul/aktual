package aktual.api.client

import aktual.api.model.banksync.BankSyncEnvelope
import aktual.api.model.banksync.BankSyncStatusResponse
import aktual.api.model.banksync.BankSyncTransactionsRequest
import aktual.api.model.banksync.BankSyncTransactionsResponse
import aktual.budget.BudgetLocalPreferences
import aktual.budget.model.AccountSyncSource
import aktual.budget.model.DbMetadata.Companion.CloudFileId
import aktual.core.model.BudgetServer
import aktual.di.BudgetScope
import dev.zacsweers.metro.ContributesBinding
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.path
import kotlinx.serialization.json.JsonObject

@ContributesBinding(BudgetScope::class)
class BankSyncApiImpl(
  @param:AktualClient private val client: HttpClient,
  private val server: BudgetServer,
  private val prefs: BudgetLocalPreferences,
) : BankSyncApi {
  override suspend fun status(source: AccountSyncSource): BankSyncStatusResponse {
    val envelope =
      client
        .post { request(source, endpoint = "status", body = JsonObject(emptyMap())) }
        .body<BankSyncEnvelope<BankSyncStatusResponse.Success>>()
    val data = envelope.data
    return if (envelope.isOk && data != null) {
      data
    } else {
      BankSyncStatusResponse.Rejected(envelope.reason, envelope.details)
    }
  }

  override suspend fun transactions(
    source: AccountSyncSource,
    request: BankSyncTransactionsRequest,
  ): BankSyncTransactionsResponse {
    val envelope =
      client
        .post { request(source, endpoint = "transactions", body = request) }
        .body<BankSyncEnvelope<BankSyncTransactionsResponse>>()
    val data = envelope.data
    return if (envelope.isOk && data != null) {
      data
    } else {
      BankSyncTransactionsResponse.Rejected(envelope.reason, envelope.details)
    }
  }

  private inline fun <reified T : Any> HttpRequestBuilder.request(
    source: AccountSyncSource,
    endpoint: String,
    body: T,
  ) {
    val remote = checkNotNull(server as? BudgetServer.Remote) { "No server for bank sync" }
    url {
      protocol = remote.url.protocol()
      host = remote.url.baseUrl
      path(source.serverPath(), endpoint)
    }
    header(AktualHeaders.TOKEN, remote.token)

    // Pluggy.ai credentials can be set per budget file, so the server needs to know which file
    if (source == AccountSyncSource.PluggyAi) {
      prefs[CloudFileId]?.let { header(AktualHeaders.FILE_ID, it) }
    }

    // Rejections like an unconfigured provider come back as 4xx with the usual JSON envelope
    expectSuccess = false
    contentType(ContentType.Application.Json)
    setBody(body)
  }

  // packages/sync-server/src/app.ts
  private fun AccountSyncSource.serverPath(): String =
    when (this) {
      AccountSyncSource.GoCardless -> "gocardless"
      AccountSyncSource.SimpleFin -> "simplefin"
      AccountSyncSource.PluggyAi -> "pluggyai"
      AccountSyncSource.Akahu -> "akahu"
      AccountSyncSource.EnableBanking -> "enablebanking"
      else -> throw IllegalArgumentException("Unsupported bank sync source $this")
    }
}
