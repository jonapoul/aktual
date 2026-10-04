package aktual.api.client

import aktual.api.model.banksync.BankSyncEnvelope
import aktual.api.model.banksync.BankSyncTransactionsResponse
import aktual.api.model.banksync.BankSyncTransactionsResponse.ProviderError
import aktual.api.model.banksync.EnableBankingAccountType
import aktual.api.model.banksync.EnableBankingAccountsResponse
import aktual.api.model.banksync.EnableBankingBank
import aktual.api.model.banksync.EnableBankingBanksResponse
import aktual.api.model.banksync.EnableBankingLoginResponse
import aktual.core.model.AktualJson
import aktual.core.model.BudgetServer
import aktual.di.BudgetScope
import dev.zacsweers.metro.ContributesBinding
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

@ContributesBinding(BudgetScope::class)
class EnableBankingApiImpl(
  @param:AktualClient private val client: HttpClient,
  private val server: BudgetServer,
) : EnableBankingApi {
  override suspend fun banks(country: String): EnableBankingBanksResponse {
    val body = buildJsonObject { put(COUNTRY, country) }
    val envelope = post("aspsps", body, REQUEST_TIMEOUT)
    val data = envelope.data as? JsonObject
    val banks = data?.get("aspsps") as? JsonArray
    return when (val failure = envelope.failure()) {
      null if banks != null ->
        EnableBankingBanksResponse.Success(AktualJson.decodeFromJsonElement(banks))
      else -> EnableBankingBanksResponse.Failed(failure ?: envelope.rejected())
    }
  }

  override suspend fun login(
    bank: EnableBankingBank,
    type: EnableBankingAccountType,
  ): EnableBankingLoginResponse {
    val body = buildJsonObject {
      putJsonObject("aspsp") {
        put("name", bank.name)
        put(COUNTRY, bank.country)
      }
      // The server's page that closes the browser tab once the user's logged in. It has to be
      // registered with the user's Enable Banking application
      put(
        "redirectUrl",
        "${server.remote().url.toString().trimEnd('/')}/enablebanking/auth_callback",
      )
      bank.maxConsentValidity?.let { put("maxConsentValidity", it) }
      put("psuType", type.value)
    }
    val envelope = post("start-auth", body, REQUEST_TIMEOUT)
    val data = envelope.data as? JsonObject
    return when (val failure = envelope.failure()) {
      null if data != null ->
        AktualJson.decodeFromJsonElement<EnableBankingLoginResponse.Success>(data)
      else -> EnableBankingLoginResponse.Failed(failure ?: envelope.rejected())
    }
  }

  override suspend fun accounts(state: String): EnableBankingAccountsResponse {
    val body = buildJsonObject { put("state", state) }
    val envelope = post("poll-auth", body, POLL_TIMEOUT)
    val data = envelope.data as? JsonObject
    val accounts = data?.get("accounts") as? JsonArray
    val error = (data?.get(ERROR) as? JsonPrimitive)?.contentOrNull
    return when (val failure = envelope.failure()) {
      null if accounts != null ->
        EnableBankingAccountsResponse.Success(enableBankingAccounts(accounts))
      // The server stops waiting after five minutes
      null if error == POLL_TIMED_OUT -> EnableBankingAccountsResponse.Failed(timedOut())
      else -> EnableBankingAccountsResponse.Failed(failure ?: envelope.rejected())
    }
  }

  private suspend fun post(
    endpoint: String,
    body: JsonObject,
    timeout: Duration,
  ): BankSyncEnvelope<JsonElement> =
    timingOut {
      client
        .post {
          bankSyncRequest(server.remote(), EnableBanking, endpoint, body, timeout)
        }
        .body<BankSyncEnvelope<JsonElement>>()
    } ?: BankSyncEnvelope(status = "ok", data = AktualJson.encodeToJsonElement(timedOut()))

  // Fails with either a provider error or a bare message, besides a rejection
  private fun BankSyncEnvelope<JsonElement>.failure(): BankSyncTransactionsResponse.Failure? {
    val data = data as? JsonObject
    val error = (data?.get(ERROR) as? JsonPrimitive)?.contentOrNull
    return when {
      data != null && "error_code" in data -> AktualJson.decodeFromJsonElement<ProviderError>(data)
      !isOk -> rejected()
      error != null && error != POLL_TIMED_OUT ->
        BankSyncTransactionsResponse.Rejected(error, details = null)
      else -> null
    }
  }

  private fun BankSyncEnvelope<*>.rejected() =
    BankSyncTransactionsResponse.Rejected(reason, details)

  private fun timedOut() = ProviderError(ProviderError.TIMED_OUT, ProviderError.TIMED_OUT)

  private companion object {
    val REQUEST_TIMEOUT = 1.minutes

    // A little longer than the server waits for a login, as upstream
    val POLL_TIMEOUT = 310.seconds

    const val COUNTRY = "country"
    const val ERROR = "error"
    const val POLL_TIMED_OUT = "Polling timed out"
  }
}
