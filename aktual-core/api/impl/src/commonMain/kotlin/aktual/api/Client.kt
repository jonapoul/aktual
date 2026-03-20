package aktual.api

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.LoggingFormat
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import logcat.logcat

fun buildKtorClient(
  json: Json,
  tag: String?,
  engine: HttpClientEngine,
  isDebug: Boolean = false,
  extraConfig: HttpClientConfig<*>.() -> Unit = {},
): HttpClient =
  HttpClient(engine) {
    // Throws ResponseExceptions for non-2xx responses
    expectSuccess = true

    install(ContentNegotiation) { json(json) }

    if (isDebug) {
      install(Logging) {
        logger = AktualKtorLogger(tag)
        level = LogLevel.HEADERS
        format = LoggingFormat.OkHttp
        // filter { request -> true }
        // sanitizeHeader { header -> header == HttpHeaders.Authorization }
      }
    }

    extraConfig()
  }

private class AktualKtorLogger(private val tag: String?) : Logger {
  override fun log(message: String) = logcat.v(tag) { message }
}
