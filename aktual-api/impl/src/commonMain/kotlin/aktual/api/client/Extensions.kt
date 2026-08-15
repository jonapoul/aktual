package aktual.api.client

import aktual.core.model.ServerUrl
import io.ktor.http.URLProtocol

internal fun ServerUrl.protocol(): URLProtocol =
  when (protocol) {
    Http -> URLProtocol.HTTP
    Https -> URLProtocol.HTTPS
  }
