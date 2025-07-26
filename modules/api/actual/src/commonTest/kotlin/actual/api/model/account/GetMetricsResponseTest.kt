package actual.api.model.account

import actual.api.model.metrics.GetMetricsResponse
import actual.core.model.bytes
import actual.test.MetricsResponses
import actual.test.testDecoding
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds

class GetMetricsResponseTest {
  @Test
  fun `Decode from JSON`() = testDecoding(
    json = MetricsResponses.METRICS_SUCCESS_200,
    expected = GetMetricsResponse(
      uptime = 806662.711204594.seconds,
      memory = GetMetricsResponse.Memory(
        rss = 112377856L.bytes,
        heapTotal = 32088064L.bytes,
        heapUsed = 29558608L.bytes,
        external = 3925760L.bytes,
        arrayBuffers = 387791L.bytes,
      ),
    ),
  )
}
