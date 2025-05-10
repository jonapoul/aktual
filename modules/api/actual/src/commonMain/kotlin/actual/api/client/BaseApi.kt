package actual.api.client

import actual.api.model.base.InfoResponse
import actual.url.model.ServerUrl
import io.ktor.client.HttpClient
import io.ktor.client.call.body

interface BaseApi {
  suspend fun info(): InfoResponse
}

fun BaseApi(url: ServerUrl, client: HttpClient): BaseApi = BaseApiClient(url, client)

private class BaseApiClient(private val serverUrl: ServerUrl, private val client: HttpClient) : BaseApi {
  override suspend fun info() = client
    .get(serverUrl, path = "/info")
    .body<InfoResponse>()
}
