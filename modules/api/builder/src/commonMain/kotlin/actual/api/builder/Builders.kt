package actual.api.builder

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.LoggingFormat
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import alakazam.kotlin.logging.Logger as AlakazamLogger
import io.ktor.client.plugins.logging.Logger as KtorLogger

fun buildKtorClient(
  json: Json,
  tag: String?,
  engine: HttpClientEngine = CIO.create(),
  isDebug: Boolean = false,
) = HttpClient(engine) {
  // Throws ResponseExceptions for non-2xx responses
  expectSuccess = true

  install(ContentNegotiation) {
    json(json)
  }

  if (isDebug) {
    install(Logging) {
      logger = ActualLogger(tag)
      level = LogLevel.ALL
      format = LoggingFormat.OkHttp
      // filter { request -> true }
      // sanitizeHeader { header -> header == HttpHeaders.Authorization }
    }
  }
}

private class ActualLogger(private val tag: String?) : KtorLogger {
  private val delegate = if (tag == null) AlakazamLogger else AlakazamLogger.tag(tag)
  override fun log(message: String) = delegate.v(message)
}
