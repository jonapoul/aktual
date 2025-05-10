package actual.api.client

import actual.url.model.Protocol
import actual.url.model.ServerUrl
import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.URLProtocol
import io.ktor.http.contentType
import io.ktor.http.path

internal typealias HttpMethodLambda = suspend HttpClient.(HttpRequestBuilder.() -> Unit) -> HttpResponse

internal suspend fun HttpClient.request(
  serverUrl: ServerUrl,
  path: String,
  headers: Map<String, String>,
  method: HttpMethodLambda,
  extraConfig: HttpRequestBuilder.() -> Unit = {},
) = method {
  url {
    protocol = serverUrl.urlProtocol()
    host = serverUrl.baseUrl
    path(path)
  }
  headers.forEach { (name, value) -> header(name, value) }
  extraConfig()
}

internal suspend fun HttpClient.get(
  serverUrl: ServerUrl,
  path: String,
  headers: Map<String, String> = emptyMap(),
) = request(serverUrl, path, headers, method = { get(it) })

internal suspend fun HttpClient.post(
  serverUrl: ServerUrl,
  path: String,
  headers: Map<String, String> = emptyMap(),
) = request(
  serverUrl = serverUrl,
  path = path,
  headers = headers,
  method = { post(it) },
)

internal suspend inline fun <reified T> HttpClient.post(
  serverUrl: ServerUrl,
  body: T,
  path: String,
  headers: Map<String, String> = emptyMap(),
  contentType: ContentType = ContentType.Application.Json,
) = request(
  serverUrl = serverUrl,
  path = path,
  headers = headers,
  method = { post(it) },
  extraConfig = {
    contentType(contentType)
    setBody(body)
  },
)

private fun ServerUrl.urlProtocol() = when (protocol) {
  Protocol.Http -> URLProtocol.HTTP
  Protocol.Https -> URLProtocol.HTTPS
}
