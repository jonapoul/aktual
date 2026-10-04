package aktual.api.client

import aktual.api.model.banksync.BankSyncEnvelope
import aktual.api.model.banksync.BankSyncSecret
import aktual.api.model.banksync.SecretResponse
import aktual.api.model.banksync.SecretResponse.Failed
import aktual.core.model.BudgetServer
import aktual.di.BudgetScope
import dev.zacsweers.metro.ContributesBinding
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.delete
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType.Application.Json
import io.ktor.http.contentType
import io.ktor.http.path
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@ContributesBinding(BudgetScope::class)
class SecretsApiImpl(
  @param:AktualClient private val client: HttpClient,
  private val server: BudgetServer,
) : SecretsApi {
  override suspend fun set(secret: BankSyncSecret, value: String?): SecretResponse {
    val response =
      if (value == null) {
        client.delete { request(SECRET, secret.value) }
      } else {
        client.post {
          request(SECRET)
          contentType(Json)
          setBody(
            buildJsonObject {
              put("name", secret.value)
              put("value", value)
            }
          )
        }
      }
    val envelope = response.body<BankSyncEnvelope<JsonElement>>()
    return if (envelope.isOk) {
      Success
    } else {
      Failed(envelope.reason, envelope.details)
    }
  }

  private fun HttpRequestBuilder.request(vararg segments: String) {
    val remote = server.remote()
    url {
      protocol = remote.url.protocol()
      host = remote.url.baseUrl
      path(*segments)
    }
    header(AktualHeaders.TOKEN, remote.token)
    // Refusals like a user who isn't an admin come back as 4xx with the usual JSON envelope
    expectSuccess = false
  }

  private companion object {
    const val SECRET = "secret"
  }
}
