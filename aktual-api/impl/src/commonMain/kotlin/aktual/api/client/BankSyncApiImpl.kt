package aktual.api.client

import aktual.api.model.banksync.BankSyncEnvelope
import aktual.api.model.banksync.BankSyncStatusResponse
import aktual.api.model.banksync.BankSyncTransactionsRequest
import aktual.api.model.banksync.BankSyncTransactionsResponse
import aktual.api.model.banksync.BankSyncTransactionsResponse.ProviderError
import aktual.api.model.banksync.SimpleFinBatchRequest
import aktual.api.model.banksync.SimpleFinBatchResponse
import aktual.budget.BudgetLocalPreferences
import aktual.budget.model.AccountSyncSource
import aktual.budget.model.DbMetadata.Companion.CloudFileId
import aktual.core.model.AktualJson
import aktual.core.model.BudgetServer
import aktual.di.BudgetScope
import dev.zacsweers.metro.ContributesBinding
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.plugins.timeout
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.path
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement

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
      timingOut {
        client
          .post { request(source, TRANSACTIONS, request, timeout = ACCOUNT_TIMEOUT) }
          .body<BankSyncEnvelope<BankSyncTransactionsResponse>>()
      } ?: return ProviderError(ProviderError.TIMED_OUT, ProviderError.TIMED_OUT)
    val data = envelope.data
    return if (envelope.isOk && data != null) {
      data
    } else {
      BankSyncTransactionsResponse.Rejected(envelope.reason, envelope.details)
    }
  }

  override suspend fun simpleFinBatch(request: SimpleFinBatchRequest): SimpleFinBatchResponse {
    val envelope =
      timingOut {
        client
          .post {
            request(AccountSyncSource.SimpleFin, TRANSACTIONS, request, timeout = BATCH_TIMEOUT)
          }
          .body<BankSyncEnvelope<JsonObject>>()
      }
        ?: return SimpleFinBatchResponse.Failed(
          ProviderError(ProviderError.TIMED_OUT, ProviderError.TIMED_OUT)
        )
    val data = envelope.data
    return when {
      !envelope.isOk || data == null ->
        SimpleFinBatchResponse.Failed(
          BankSyncTransactionsResponse.Rejected(envelope.reason, envelope.details)
        )
      data.isEmpty() ->
        SimpleFinBatchResponse.Failed(ProviderError(ProviderError.NO_DATA, ProviderError.NO_DATA))
      ERROR_CODE in data ->
        SimpleFinBatchResponse.Failed(AktualJson.decodeFromJsonElement<ProviderError>(data))
      else -> SimpleFinBatchResponse.Success(batchAccounts(data))
    }
  }

  // An account's first error wins over its download, as upstream. Accounts with neither, or a
  // download without transactions, are left out
  private fun batchAccounts(data: JsonObject): Map<String, BankSyncTransactionsResponse> {
    val errors = data[ERRORS] as? JsonObject
    val ids = data.keys - ERRORS + errors?.keys.orEmpty()
    return ids
      .mapNotNull { id ->
        val error = (errors?.get(id) as? JsonArray)?.firstOrNull()
        val download = (data[id] as? JsonObject)?.takeIf { TRANSACTIONS in it }
        when {
          error != null -> id to AktualJson.decodeFromJsonElement<ProviderError>(error)
          download != null ->
            id to AktualJson.decodeFromJsonElement<BankSyncTransactionsResponse>(download)
          else -> null
        }
      }
      .toMap()
  }

  // Null if the request timed out
  private inline fun <T> timingOut(block: () -> T): T? =
    try {
      block()
    } catch (_: HttpRequestTimeoutException) {
      null
    } catch (_: SocketTimeoutException) {
      null
    } catch (_: ConnectTimeoutException) {
      null
    }

  private inline fun <reified T : Any> HttpRequestBuilder.request(
    source: AccountSyncSource,
    endpoint: String,
    body: T,
    timeout: Duration? = null,
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

    if (timeout != null) {
      timeout {
        requestTimeoutMillis = timeout.inWholeMilliseconds
        socketTimeoutMillis = timeout.inWholeMilliseconds
      }
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

  private companion object {
    // As upstream: a minute for one account, five for a SimpleFIN batch
    val ACCOUNT_TIMEOUT = 1.minutes
    val BATCH_TIMEOUT = 5.minutes

    const val ERROR_CODE = "error_code"
    const val ERRORS = "errors"
    const val TRANSACTIONS = "transactions"
  }
}
