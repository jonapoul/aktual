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
package aktual.test

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.util.toMap
import org.intellij.lang.annotations.Language

fun MockRequestHandleScope.respondJson(
  @Language("JSON") content: String,
  status: HttpStatusCode = HttpStatusCode.OK,
): HttpResponseData = respond(
  content = content,
  status = status,
  headers = headersOf(HttpHeaders.ContentType, "application/json"),
)

fun emptyMockEngine(): MockEngine.Queue = MockEngine.Queue()

fun MockEngine.clear() = config.requestHandlers.clear()

fun MockEngine.latestRequest() = requestHistory.last()

fun MockEngine.latestRequestHeaders() = latestRequest().headers.toMap()

fun MockEngine.latestRequestUrl() = latestRequest().url.toString()

fun MockEngine.Queue.enqueueResponse(content: ByteArray) = enqueue {
  respond(
    content = content,
    status = HttpStatusCode.OK,
    headers = headersOf("Content-Length", content.size.toString()),
  )
}

fun MockEngine.Queue.enqueueResponse(content: String) = enqueueResponse(content.toByteArray())
