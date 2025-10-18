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
package actual.api.client

import actual.api.model.health.GetHealthResponse
import actual.test.HealthResponses
import actual.test.emptyMockEngine
import actual.test.respondJson
import actual.test.testHttpClient
import assertk.assertThat
import assertk.assertions.isEqualTo
import io.ktor.client.engine.mock.MockEngine
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class HealthApiTest {
  private lateinit var mockEngine: MockEngine.Queue
  private lateinit var healthApi: HealthApi

  @BeforeTest
  fun before() {
    mockEngine = emptyMockEngine()
    healthApi = HealthApi(SERVER_URL, testHttpClient(mockEngine, ActualJson))
  }

  @AfterTest
  fun after() {
    mockEngine.close()
  }

  @Test
  fun `Get health successfully`() = runTest {
    mockEngine += { respondJson(HealthResponses.HEALTH_SUCCESS_200) }
    assertThat(healthApi.getHealth()).isEqualTo(GetHealthResponse(status = "UP"))
  }
}
