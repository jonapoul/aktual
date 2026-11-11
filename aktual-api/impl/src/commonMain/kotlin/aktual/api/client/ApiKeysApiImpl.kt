package aktual.api.client

import aktual.api.model.apikeys.ApiKey
import aktual.api.model.apikeys.ApiKeyId
import aktual.api.model.apikeys.ApiKeysResponse
import aktual.api.model.apikeys.CreateApiKeyRequest
import aktual.api.model.apikeys.CreatedApiKey
import aktual.api.model.banksync.BankSyncEnvelope
import aktual.budget.model.BudgetId
import aktual.core.model.BudgetServer
import aktual.di.BudgetScope
import dev.zacsweers.metro.ContributesBinding
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType.Application.Json
import io.ktor.http.contentType
import io.ktor.http.path

@ContributesBinding(BudgetScope::class)
class ApiKeysApiImpl
internal constructor(
  @param:AktualClient private val client: HttpClient,
  private val server: BudgetServer,
) : ApiKeysApi {
  override suspend fun list(): ApiKeysResponse<List<ApiKey>> =
    parse<List<ApiKey>>(client.get { request() })

  override suspend fun create(
    name: String,
    budgets: List<BudgetId>,
  ): ApiKeysResponse<CreatedApiKey> {
    val response = client.post {
      request()
      contentType(Json)
      setBody(CreateApiKeyRequest(name, budgets))
    }
    return parse<CreatedApiKey>(response)
  }

  // A successful revoke has no data
  override suspend fun revoke(id: ApiKeyId): ApiKeysResponse<Unit> =
    parse<Unit>(client.delete { request(id.value) }, emptyData = Unit)

  private suspend inline fun <reified T> parse(
    response: HttpResponse,
    emptyData: T? = null,
  ): ApiKeysResponse<T> {
    // Servers without these endpoints serve the web app's HTML instead
    if (response.contentType()?.match(Json) != true) {
      return ApiKeysResponse.Failed(ApiKeysResponse.UNSUPPORTED)
    }
    val envelope = response.body<BankSyncEnvelope<T>>()
    val data = envelope.data ?: emptyData
    return if (envelope.isOk && data != null) {
      ApiKeysResponse.Success(data)
    } else {
      ApiKeysResponse.Failed(envelope.reason)
    }
  }

  private fun HttpRequestBuilder.request(vararg segments: String) {
    val remote = server.remote()
    url {
      protocol = remote.url.protocol()
      host = remote.url.baseUrl
      path(API_KEYS, *segments)
    }
    header(AktualHeaders.TOKEN, remote.token)
    // Refusals like an invalid name come back as 4xx with the usual JSON envelope
    expectSuccess = false
  }

  private companion object {
    const val API_KEYS = "api-keys"
  }
}
