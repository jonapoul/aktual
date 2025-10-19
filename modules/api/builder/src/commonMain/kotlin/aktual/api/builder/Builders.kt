/**
 * Copyright 2025 Jon Poulton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
