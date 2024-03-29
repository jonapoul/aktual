package dev.jonpoulton.actual.test.http

import dev.jonpoulton.actual.core.model.ServerUrl
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.rules.TestWatcher
import org.junit.runner.Description

class MockWebServerRule : TestWatcher() {
  private lateinit var server: MockWebServer

  override fun starting(description: Description?) {
    super.starting(description)
    server = MockWebServer()
  }

  override fun finished(description: Description?) {
    super.finished(description)
    server.shutdown()
  }

  fun serverUrl(path: String = "/"): ServerUrl {
    return ServerUrl(server.url(path).toString())
  }

  fun enqueue(body: String, code: Int = 200) {
    enqueue {
      setBody(body)
      setResponseCode(code)
    }
  }

  fun enqueue(builder: MockResponse.() -> MockResponse) {
    enqueue(MockResponse().apply { builder() })
  }

  fun enqueue(response: MockResponse) {
    server.enqueue(response)
  }
}
