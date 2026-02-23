package aktual.api.client

import aktual.api.model.metrics.GetMetricsResponse
import aktual.core.model.bytes
import aktual.test.MetricsResponses
import aktual.test.emptyMockEngine
import aktual.test.latestRequestHeaders
import aktual.test.latestRequestUrl
import aktual.test.respondJson
import aktual.test.testHttpClient
import assertk.assertThat
import assertk.assertions.isEqualTo
import io.ktor.client.engine.mock.MockEngine
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.test.runTest

class MetricsApiTest {
  private lateinit var mockEngine: MockEngine.Queue
  private lateinit var metricsApi: MetricsApi

  @BeforeTest
  fun before() {
    mockEngine = emptyMockEngine()
    metricsApi = MetricsApi(SERVER_URL, testHttpClient(mockEngine))
  }

  @AfterTest
  fun after() {
    mockEngine.close()
  }

  @Test
  fun `Parse metrics response`() = runTest {
    // given
    mockEngine += { respondJson(MetricsResponses.METRICS_SUCCESS_200) }

    // when
    val response = metricsApi.getMetrics()

    // then the right request was sent
    assertThat(mockEngine.latestRequestUrl()).isEqualTo("https://test.server.com/metrics")
    val headers = mockEngine.latestRequestHeaders()
    assertThat(headers["Accept"]).isEqualTo(listOf("application/json"))
    assertThat(headers["Accept-Charset"]).isEqualTo(listOf("UTF-8"))

    // and the response was parsed properly
    assertThat(response)
      .isEqualTo(
        GetMetricsResponse(
          uptime = 806_662.711204594.seconds,
          memory =
            GetMetricsResponse.Memory(
              rss = 112_377_856L.bytes,
              heapTotal = 32_088_064L.bytes,
              heapUsed = 29_558_608L.bytes,
              external = 3_925_760L.bytes,
              arrayBuffers = 387_791L.bytes,
            ),
        )
      )
  }
}
