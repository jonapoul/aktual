package aktual.api.client

import aktual.core.model.ServerUrl
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.http.URLProtocol
import io.ktor.http.path

internal fun HttpRequestBuilder.url(server: ServerUrl, vararg path: String) = url {
  protocol = server.protocol()
  host = server.baseUrl
  path(*path)
}

private fun ServerUrl.protocol(): URLProtocol =
  when (protocol) {
    Http -> HTTP
    Https -> HTTPS
  }
