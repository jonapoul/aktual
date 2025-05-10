package actual.api.client

import actual.account.model.LoginToken
import actual.api.model.internal.ActualHeaders
import actual.api.model.sync.GetUserKeyRequest
import actual.api.model.sync.GetUserKeyResponse
import actual.api.model.sync.ListUserFilesResponse
import actual.budget.model.BudgetId
import actual.test.respondJson
import actual.test.testHttpClient
import actual.url.model.Protocol
import actual.url.model.ServerUrl
import alakazam.test.core.assertThrows
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.uuid.Uuid

class SyncApiTest {
  private lateinit var mockEngine: MockEngine
  private lateinit var syncApi: SyncApi

  @After
  fun after() {
    mockEngine.close()
  }

  private fun buildApi(handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData) {
    mockEngine = MockEngine(handler)
    syncApi = SyncApi(SERVER_URL, testHttpClient(mockEngine, ActualJson))
  }

  @Test
  fun `List budgets token included in header`() = runTest {
    // given
    buildApi { respondJson(SYNC_LIST_BUDGETS_SUCCESS) }

    // when
    syncApi.fetchUserFiles(TOKEN)
    val request = mockEngine.requestHistory.last()

    // then
    assertEquals(
      expected = "abc-123",
      actual = request.headers[ActualHeaders.TOKEN],
    )
  }

  @Test
  fun `List budgets success response`() = runTest {
    // given
    buildApi { respondJson(SYNC_LIST_BUDGETS_SUCCESS) }

    // when
    val response = syncApi.fetchUserFiles(TOKEN)

    // then
    assertEquals(
      actual = response,
      expected = ListUserFilesResponse.Success(
        data = listOf(
          ListUserFilesResponse.Item(
            deleted = 0,
            fileId = BudgetId("b328186c-c919-4333-959b-04e676c1ee46"),
            groupId = "afb25fc0-a294-4f71-ae8f-ce1e3a8fec10",
            name = "Main Budget",
            encryptKeyId = "2a66f4de-c530-4c06-8103-a48e26a0ce44",
            owner = null,
            usersWithAccess = emptyList(),
          ),
          ListUserFilesResponse.Item(
            deleted = 0,
            fileId = BudgetId("cf2b43ee-8067-48ed-ab5b-4e4e5531056e"),
            groupId = "ee90358a-f73e-4aa5-a922-653190fd31b7",
            name = "Other Budget",
            encryptKeyId = null,
            owner = "354d0cfe-cd36-44eb-a404-5990a3ab5c39",
            usersWithAccess = listOf(
              ListUserFilesResponse.UserWithAccess(
                userId = "354d0cfe-cd36-44eb-a404-5990a3ab5c39",
                displayName = "",
                userName = "",
                isOwner = true,
              ),
            ),
          ),
        ),
      ),
    )
  }

  @Test
  fun `List budgets failure response`() = runTest {
    // given
    buildApi { respondJson(SYNC_LIST_BUDGETS_FAILURE, HttpStatusCode.BadRequest) }

    // when
    assertThrows<ClientRequestException> { syncApi.fetchUserFiles(TOKEN) }
  }

  @Test
  fun `Get user key success`() = runTest {
    // given
    buildApi { respondJson(SYNC_GET_USER_KEY_SUCCESS) }

    // when
    val body = GetUserKeyRequest(BUDGET_ID)
    val response = syncApi.fetchUserKey(TOKEN, body)

    // then
    val keyId = Uuid.parse("2a66f4de-c530-4c06-8103-a48e26a0ce44")
    assertEquals(
      actual = response,
      expected = GetUserKeyResponse.Success(
        data = GetUserKeyResponse.Data(
          id = keyId,
          salt = "PpZ/z6DD6xtjF89wxZOszZ6CkKXNDoBXdtBlIztmneE=",
          test = GetUserKeyResponse.Test(
            value = "nrhpJgUnl8lZvWxSRMIT0aTRKCOHeddlIuGPfNw0NQR/d81m/ZYRqaOjMwoQHpduSzuAivfVZZEslZihl8WhOs7GVkdghw" +
              "Cjqr083G0261M464wHvQl2v5sB+l8f0/mQE2fco7zUagbA7Q==",
            meta = GetUserKeyResponse.Meta(
              keyId = keyId,
              algorithm = "aes-256-gcm",
              iv = "whBQQkPM88iFsLVk",
              authTag = "X/JX1lWCchd0Ekjthlxuzg==",
            ),
          ),
        ),
      ),
    )
  }

  private companion object {
    val TOKEN = LoginToken("abc-123")
    val BUDGET_ID = BudgetId("xyz-789")
    val SERVER_URL = ServerUrl(Protocol.Https, "test.unused.com")
  }
}
