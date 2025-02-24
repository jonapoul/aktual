package actual.test

import actual.api.builder.buildOkHttp
import actual.api.builder.buildRetrofit
import actual.url.model.ServerUrl
import alakazam.kotlin.core.EmptyLogger
import alakazam.kotlin.core.Logger
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.Buffer
import okio.buffer
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import retrofit2.create

class MockWebServerRule : TestWatcher() {
  lateinit var server: MockWebServer

  override fun starting(description: Description?) {
    super.starting(description)
    server = MockWebServer()
  }

  override fun finished(description: Description?) {
    super.finished(description)
    server.shutdown()
  }

  fun serverUrl(path: String = "/"): ServerUrl = ServerUrl(server.url(path).toString())

  fun enqueue(body: String, code: Int = 200) = enqueue {
    setBody(body)
    setResponseCode(code)
  }

  fun enqueue(body: ByteArray, code: Int = 200) = enqueue {
    val buffer = Buffer()
    buffer.write(body)
    setBody(buffer)
    setResponseCode(code)
  }

  fun enqueue(builder: MockResponse.() -> MockResponse) = enqueue(MockResponse().apply { builder() })

  fun enqueue(response: MockResponse) = server.enqueue(response)

  inline fun <reified T : Any> buildApi(
    json: Json = Json,
    logger: Logger = EmptyLogger,
    tag: String = "TEST",
    client: OkHttpClient = buildOkHttp(logger, isDebug = true, tag),
  ): T {
    val retrofit = buildRetrofit(
      client = client,
      url = server.url(path = "/").toString().let(::ServerUrl),
      json = json,
    )
    return retrofit.create<T>()
  }
}
