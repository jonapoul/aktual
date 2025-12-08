package aktual.api.client

import aktual.api.model.base.Build
import aktual.api.model.base.InfoResponse
import aktual.test.BaseResponses
import aktual.test.emptyMockEngine
import aktual.test.latestRequestHeaders
import aktual.test.latestRequestUrl
import aktual.test.respondJson
import aktual.test.testHttpClient
import alakazam.test.core.assertThrows
import assertk.assertThat
import assertk.assertions.isEqualTo
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondError
import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class BaseApiTest {
  private lateinit var mockEngine: MockEngine.Queue
  private lateinit var baseApi: BaseApi

  @BeforeTest
  fun before() {
    mockEngine = emptyMockEngine()
    baseApi = BaseApi(SERVER_URL, testHttpClient(mockEngine, AktualJson))
  }

  @AfterTest
  fun after() {
    mockEngine.close()
  }

  @Test
  fun `Fetch info request format`() = runTest {
    mockEngine += { respondJson(BaseResponses.INFO_SUCCESS_200) }
    baseApi.fetchInfo()

    assertThat(mockEngine.latestRequestUrl())
      .isEqualTo("https://test.server.com/info")

    assertThat(mockEngine.latestRequestHeaders())
      .isEqualTo(mapOf("Accept" to listOf("application/json"), "Accept-Charset" to listOf("UTF-8")))
  }

  @Test
  fun `Fetch info success response`() = runTest {
    // given
    mockEngine += { respondJson(BaseResponses.INFO_SUCCESS_200) }

    // when
    val response = baseApi.fetchInfo()

    // then
    assertThat(response).isEqualTo(
      InfoResponse(
        build = Build(
          name = "@actual-app/sync-server",
          description = "actual syncing server",
          version = "25.7.1",
        ),
      ),
    )
  }

  @Test
  fun `Fetch info failure response`() = runTest {
    // given
    mockEngine += { respondError(HttpStatusCode.BadRequest) }

    // when
    assertThrows<ClientRequestException> { baseApi.fetchInfo() }
  }
}
