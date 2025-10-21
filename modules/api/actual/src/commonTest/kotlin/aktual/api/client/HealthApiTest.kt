/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.api.client

import aktual.api.model.health.GetHealthResponse
import aktual.test.HealthResponses
import aktual.test.emptyMockEngine
import aktual.test.respondJson
import aktual.test.testHttpClient
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
    healthApi = HealthApi(SERVER_URL, testHttpClient(mockEngine, AktualJson))
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
