package aktual.core.utils

import aktual.core.model.ServerUrl
import java.net.URI
import java.net.URISyntaxException

// The host part of the base URL, without any port, path, query, user info or IPv6 brackets. Null
// when it doesn't parse
val ServerUrl.host: String?
  get() {
    val uri =
      try {
        URI("http://${baseUrl.trim()}")
      } catch (_: URISyntaxException) {
        return null
      }
    // URI leaves host null for names it won't take as hostnames, like ones with underscores
    return uri.host?.removeSurrounding("[", "]")
      ?: uri.rawAuthority?.substringAfterLast('@')?.substringBefore(':')
  }
