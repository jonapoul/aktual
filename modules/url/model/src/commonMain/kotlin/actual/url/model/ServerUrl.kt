package actual.url.model

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Immutable
data class ServerUrl(
  val protocol: Protocol,
  val baseUrl: String,
) {
  init {
    require(baseUrl.isNotBlank()) { "Base URL is blank" }
  }

  @Stable
  override fun toString(): String = "$protocol://$baseUrl"

  fun interface Provider {
    fun default(): ServerUrl?
  }

  companion object {
    val Demo = ServerUrl(protocol = Protocol.Https, baseUrl = "demo.actualbudget.org")
  }
}

fun ServerUrl(string: String): ServerUrl {
  val split = string.split("://")
  require(split.size == 2) { "Need a URL in format 'PROTOCOL://BASE_URL', got $string" }
  val protocol = Protocol.fromString(split[0])
  val baseUrl = split[1]
  return ServerUrl(protocol, baseUrl)
}
