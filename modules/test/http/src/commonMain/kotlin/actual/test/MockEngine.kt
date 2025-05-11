package actual.test

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.MockRequestHandler
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

fun emptyMockEngine(): MockEngine = MockEngine(ThrowingRequestHandler).also { it.clear() }

fun MockEngine.enqueue(handler: MockRequestHandler) = config.requestHandlers.add(handler)

fun MockEngine.clear() = config.requestHandlers.clear()

fun MockEngine.latestRequest() = requestHistory.last()

fun MockEngine.latestRequestHeaders() = latestRequest().headers.toMap()

fun MockEngine.latestRequestUrl() = latestRequest().url.toString()

val ThrowingRequestHandler: MockRequestHandler = { error("No-op") }
