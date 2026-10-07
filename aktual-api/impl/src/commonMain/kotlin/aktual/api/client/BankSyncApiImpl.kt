package aktual.api.client

import aktual.api.model.banksync.BankSyncAccountsResponse
import aktual.api.model.banksync.BankSyncEnvelope
import aktual.api.model.banksync.BankSyncStatusResponse
import aktual.api.model.banksync.BankSyncTransactionsRequest
import aktual.api.model.banksync.BankSyncTransactionsResponse
import aktual.api.model.banksync.BankSyncTransactionsResponse.ProviderError
import aktual.api.model.banksync.GoCardlessAccountsResponse
import aktual.api.model.banksync.GoCardlessBanksResponse
import aktual.api.model.banksync.GoCardlessLoginResponse
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
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.http.HttpHeaders
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.put

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
            request(SimpleFin, TRANSACTIONS, request, timeout = BATCH_TIMEOUT)
          }
          .body<BankSyncEnvelope<JsonObject>>()
      }
        ?: return SimpleFinBatchResponse.Failed(
          ProviderError(ProviderError.TIMED_OUT, ProviderError.TIMED_OUT),
        )
    val data = envelope.data
    return when {
      !envelope.isOk || data == null ->
        SimpleFinBatchResponse.Failed(
          BankSyncTransactionsResponse.Rejected(envelope.reason, envelope.details),
        )
      data.isEmpty() ->
        SimpleFinBatchResponse.Failed(ProviderError(ProviderError.NO_DATA, ProviderError.NO_DATA))
      ERROR_CODE in data ->
        SimpleFinBatchResponse.Failed(AktualJson.decodeFromJsonElement<ProviderError>(data))
      else -> SimpleFinBatchResponse.Success(batchAccounts(data))
    }
  }

  override suspend fun accounts(source: AccountSyncSource): BankSyncAccountsResponse {
    val envelope =
      timingOut {
        client
          .post { request(source, ACCOUNTS, JsonObject(emptyMap()), timeout = ACCOUNT_TIMEOUT) }
          .body<BankSyncEnvelope<JsonObject>>()
      }
        ?: return BankSyncAccountsResponse.Failed(
          ProviderError(ProviderError.TIMED_OUT, ProviderError.TIMED_OUT),
        )
    val data = envelope.data
    // Pluggy.ai and Akahu send failures as a bare message
    val error = (data?.get(ERROR) as? JsonPrimitive)?.contentOrNull
    val accounts = data?.get(ACCOUNTS) as? JsonArray
    return when {
      data != null && ERROR_CODE in data ->
        BankSyncAccountsResponse.Failed(AktualJson.decodeFromJsonElement<ProviderError>(data))
      !envelope.isOk || error != null || accounts == null ->
        BankSyncAccountsResponse.Failed(
          BankSyncTransactionsResponse.Rejected(envelope.reason ?: error, envelope.details),
        )
      else -> BankSyncAccountsResponse.Success(externalAccounts(source, accounts))
    }
  }

  override suspend fun goCardlessBanks(
    country: String,
    showDemo: Boolean,
  ): GoCardlessBanksResponse {
    val body = buildJsonObject {
      put("country", country)
      put("showDemo", showDemo)
    }
    val envelope = goCardless("get-banks", body)
    val data = envelope.data
    return when {
      data is JsonObject && ERROR_CODE in data ->
        GoCardlessBanksResponse.Failed(AktualJson.decodeFromJsonElement<ProviderError>(data))
      !envelope.isOk || data !is JsonArray -> GoCardlessBanksResponse.Failed(envelope.rejected())
      else -> GoCardlessBanksResponse.Success(AktualJson.decodeFromJsonElement(data))
    }
  }

  override suspend fun goCardlessLogin(bankId: String): GoCardlessLoginResponse {
    // The server sends the browser back to the Origin once the user's logged in. It needs to be
    // the server itself, whose /gocardless/link page closes the browser tab
    val origin = remote().url.toString()
    val body = buildJsonObject {
      put("institutionId", bankId)
      put("accessValidForDays", ACCESS_VALID_DAYS)
    }
    val envelope = goCardless("create-web-token", body) { header(HttpHeaders.Origin, origin) }
    val data = envelope.data as? JsonObject
    return when {
      data != null && ERROR_CODE in data ->
        GoCardlessLoginResponse.Failed(AktualJson.decodeFromJsonElement<ProviderError>(data))
      !envelope.isOk || data == null -> GoCardlessLoginResponse.Failed(envelope.rejected())
      else -> AktualJson.decodeFromJsonElement<GoCardlessLoginResponse.Success>(data)
    }
  }

  override suspend fun goCardlessAccounts(requisitionId: String): GoCardlessAccountsResponse {
    val body = buildJsonObject { put(REQUISITION_ID, requisitionId) }
    val envelope = goCardless("get-accounts", body)
    val data = envelope.data as? JsonObject
    val accounts = data?.get(ACCOUNTS) as? JsonArray
    val requisition = (data?.get("id") as? JsonPrimitive)?.contentOrNull
    return when {
      data != null && ERROR_CODE in data ->
        GoCardlessAccountsResponse.Failed(AktualJson.decodeFromJsonElement<ProviderError>(data))
      !envelope.isOk -> GoCardlessAccountsResponse.Failed(envelope.rejected())
      // Sent without data until the requisition's linked
      data == null -> Pending
      accounts == null || requisition == null ->
        GoCardlessAccountsResponse.Failed(envelope.rejected())
      else -> GoCardlessAccountsResponse.Success(goCardlessAccounts(requisition, accounts))
    }
  }

  override suspend fun removeGoCardlessRequisition(requisitionId: String): Boolean {
    val body = buildJsonObject { put(REQUISITION_ID, requisitionId) }
    return client
      .post { request(GoCardless, "remove-account", body) }
      .body<BankSyncEnvelope<JsonObject>>()
      .isOk
  }

  private suspend fun goCardless(
    endpoint: String,
    body: JsonObject,
    block: HttpRequestBuilder.() -> Unit = {},
  ): BankSyncEnvelope<JsonElement> =
    timingOut {
      client
        .post {
          request(GoCardless, endpoint, body, timeout = ACCOUNT_TIMEOUT)
          block()
        }
        .body<BankSyncEnvelope<JsonElement>>()
    }
      ?: BankSyncEnvelope(
        status = "ok",
        data =
          buildJsonObject {
            put(ERROR_TYPE, ProviderError.TIMED_OUT)
            put(ERROR_CODE, ProviderError.TIMED_OUT)
          },
      )

  private fun BankSyncEnvelope<*>.rejected() =
    BankSyncTransactionsResponse.Rejected(reason, details)

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

  private fun remote() = server.remote()

  private inline fun <reified T : Any> HttpRequestBuilder.request(
    source: AccountSyncSource,
    endpoint: String,
    body: T,
    timeout: Duration? = null,
  ) {
    bankSyncRequest(remote(), source, endpoint, body, timeout)

    // Pluggy.ai credentials can be set per budget file, so the server needs to know which file
    if (source == PluggyAi) {
      prefs[CloudFileId]?.let { header(AktualHeaders.FILE_ID, it) }
    }
  }

  private companion object {
    // As upstream: a minute for one account, five for a SimpleFIN batch
    val ACCOUNT_TIMEOUT = 1.minutes
    val BATCH_TIMEOUT = 5.minutes

    // packages/desktop-client/src/gocardless.ts
    const val ACCESS_VALID_DAYS = 90
    const val ACCOUNTS = "accounts"
    const val ERROR = "error"
    const val ERROR_CODE = "error_code"
    const val ERRORS = "errors"
    const val ERROR_TYPE = "error_type"
    const val REQUISITION_ID = "requisitionId"
    const val TRANSACTIONS = "transactions"
  }
}
