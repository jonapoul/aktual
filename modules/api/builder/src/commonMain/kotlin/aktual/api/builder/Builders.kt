/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.api.builder

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.LoggingFormat
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

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
      logger = AktualKtorLogger(tag)
      level = LogLevel.HEADERS
      format = LoggingFormat.OkHttp
      // filter { request -> true }
      // sanitizeHeader { header -> header == HttpHeaders.Authorization }
    }
  }
}
