/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.api.model.account

import aktual.api.model.metrics.GetMetricsResponse
import aktual.core.model.bytes
import aktual.test.MetricsResponses
import aktual.test.testDecoding
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
