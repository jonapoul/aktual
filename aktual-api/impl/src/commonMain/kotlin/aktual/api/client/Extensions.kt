package aktual.api.client

import aktual.core.model.Protocol
import aktual.core.model.ServerUrl
import io.ktor.http.URLProtocol

internal fun ServerUrl.protocol(): URLProtocol =
  when (protocol) {
    Protocol.Http -> URLProtocol.HTTP
    Protocol.Https -> URLProtocol.HTTPS
  }
