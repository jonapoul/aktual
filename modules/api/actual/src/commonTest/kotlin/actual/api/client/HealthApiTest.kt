package actual.api.client

import actual.api.model.health.GetHealthResponse
import actual.test.emptyMockEngine
import actual.test.plusAssign
import actual.test.respondJson
import actual.test.testHttpClient
import io.ktor.client.engine.mock.MockEngine
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class HealthApiTest {
  private lateinit var mockEngine: MockEngine
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
    mockEngine += { respondJson(HEALTH_SUCCESS) }
    assertEquals(
      expected = GetHealthResponse(status = "UP"),
      actual = healthApi.getHealth()
    )
  }
}
