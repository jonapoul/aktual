package actual.core.model

import alakazam.kotlin.core.parse

data class ServerUrl(
  val protocol: Protocol,
  val baseUrl: String,
) {
  init {
    require(baseUrl.isNotBlank()) { "Base URL is blank" }
  }

  override fun toString(): String = "$protocol://$baseUrl"

  companion object {
    val Demo = ServerUrl(protocol = Protocol.Https, baseUrl = "demo.actualbudget.org")
  }
}

fun ServerUrl(string: String): ServerUrl {
  val split = string.split("://")
  require(split.size == 2) { "Need a URL in format 'PROTOCOL://BASE_URL', got $string" }
  val protocol = Protocol::class.parse(split[0])
  val baseUrl = split[1]
  return ServerUrl(protocol, baseUrl)
}
