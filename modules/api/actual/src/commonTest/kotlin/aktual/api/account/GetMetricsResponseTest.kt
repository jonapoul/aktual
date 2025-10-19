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
