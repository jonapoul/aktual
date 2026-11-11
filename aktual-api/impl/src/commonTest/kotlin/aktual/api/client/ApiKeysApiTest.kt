package aktual.api.client

import aktual.api.model.apikeys.ApiKey
import aktual.api.model.apikeys.ApiKeyId
import aktual.api.model.apikeys.ApiKeysResponse
import aktual.api.model.apikeys.CreatedApiKey
import aktual.budget.model.BudgetId
import aktual.core.model.AktualJson
import aktual.core.model.BudgetServer
import aktual.core.model.Token
import aktual.test.ApiKeysResponses
import aktual.test.emptyMockEngine
import aktual.test.latestRequest
import aktual.test.latestRequestUrl
import aktual.test.respondJson
import aktual.test.testHttpClient
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.prop
import io.ktor.client.engine.mock.MockEngine
import io.ktor.http.content.TextContent
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class ApiKeysApiTest {
  private lateinit var mockEngine: MockEngine.Queue
  private lateinit var api: ApiKeysApi

  @BeforeTest
  fun before() {
    mockEngine = emptyMockEngine()
    api =
      ApiKeysApiImpl(
        client = testHttpClient(mockEngine, AktualJson),
        server = BudgetServer.Remote(SERVER_URL, TOKEN),
      )
  }

  @AfterTest
  fun after() {
    mockEngine.close()
  }

  @Test
  fun `List keys`() = runTest {
    mockEngine += { respondJson(ApiKeysResponses.LIST_SUCCESS_200) }

    val response = api.list()

    val request = mockEngine.latestRequest()
    assertThat(request.method).isEqualTo(Get)
    assertThat(request.headers[AktualHeaders.TOKEN]).isEqualTo(TOKEN.value)
    assertThat(mockEngine.latestRequestUrl()).isEqualTo("https://test.server.com/api-keys")
    assertThat(response)
      .isEqualTo(
        ApiKeysResponse.Success(
          listOf(
            ApiKey(
              id = KEY_ID,
              name = "Home Assistant",
              budgets = listOf(KEY_BUDGET),
              createdAtSeconds = 1793059200,
            ),
          ),
        ),
      )
  }

  @Test
  fun `Create key`() = runTest {
    mockEngine += { respondJson(ApiKeysResponses.CREATE_SUCCESS_201, status = Created) }

    val response = api.create(name = "Home Assistant", budgets = listOf(KEY_BUDGET))

    assertThat(mockEngine.latestRequest().method).isEqualTo(Post)
    assertThat(mockEngine.latestRequestUrl()).isEqualTo("https://test.server.com/api-keys")
    assertThat(mockEngine.latestRequest().body)
      .isInstanceOf<TextContent>()
      .prop(TextContent::text)
      .isEqualTo("""{"name":"Home Assistant","fileIds":["${KEY_BUDGET.value}"]}""")
    assertThat(response)
      .isEqualTo(
        ApiKeysResponse.Success(
          CreatedApiKey(
            id = KEY_ID,
            name = "Home Assistant",
            budgets = listOf(KEY_BUDGET),
            createdAtSeconds = 1793059200,
            key = Token("actual_q7Xc2mVb9LpR4tYw8NzK3hJf6Gd1Sa5Ue0Io-_TnBvM"),
          ),
        ),
      )
  }

  @Test
  fun `Create refusal`() = runTest {
    mockEngine += { respondJson(ApiKeysResponses.CREATE_INVALID_400, status = BadRequest) }

    val response = api.create(name = "", budgets = emptyList())

    assertThat(response).isEqualTo(ApiKeysResponse.Failed(ApiKeysResponse.INVALID_API_KEY))
  }

  @Test
  fun `Revoke key`() = runTest {
    mockEngine += { respondJson(ApiKeysResponses.REVOKE_SUCCESS_200) }

    val response = api.revoke(KEY_ID)

    assertThat(mockEngine.latestRequest().method).isEqualTo(Delete)
    assertThat(mockEngine.latestRequestUrl())
      .isEqualTo("https://test.server.com/api-keys/${KEY_ID.value}")
    assertThat(response).isEqualTo(ApiKeysResponse.Success(Unit))
  }

  @Test
  fun `Revoke missing key`() = runTest {
    mockEngine += { respondJson(ApiKeysResponses.REVOKE_NOT_FOUND_404, status = NotFound) }

    val response = api.revoke(KEY_ID)

    assertThat(response).isEqualTo(ApiKeysResponse.Failed(ApiKeysResponse.API_KEY_NOT_FOUND))
  }

  private companion object {
    val KEY_ID = ApiKeyId("8b1f0c3e-5a7d-4f2b-9c61-2e4d8a9f7b10")
    val KEY_BUDGET = BudgetId("c4b9b5f0-7d3a-4e8c-a1f2-6b0d9e3c5a71")
  }
}
