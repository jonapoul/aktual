package aktual.api.client

import aktual.api.model.base.InfoResponse
import aktual.core.model.ServerUrl
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.path

class BaseApiImpl(private val client: HttpClient, private val serverUrl: ServerUrl) : BaseApi {
  private val urlProtocol = serverUrl.protocol()

  override suspend fun fetchInfo(): InfoResponse =
    client
      .`get` {
        url {
          protocol = urlProtocol
          host = serverUrl.baseUrl
          path("/info")
        }
      }
      .body<InfoResponse>()
}
