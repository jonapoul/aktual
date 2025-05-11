package actual.api.client

import actual.api.model.account.FailureReason
import actual.api.model.sync.EncryptMeta
import actual.api.model.sync.GetUserFileInfoResponse
import actual.api.model.sync.GetUserKeyRequest
import actual.api.model.sync.GetUserKeyResponse
import actual.api.model.sync.ListUserFilesResponse
import actual.api.model.sync.UserFile
import actual.api.model.sync.UserWithAccess
import actual.budget.model.BudgetId
import actual.test.emptyMockEngine
import actual.test.enqueue
import actual.test.latestRequestHeaders
import actual.test.respondJson
import actual.test.testHttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondOk
import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.util.toMap
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.fail
import kotlin.uuid.Uuid

class SyncApiTest {
  private lateinit var mockEngine: MockEngine
  private lateinit var syncApi: SyncApi

  @BeforeTest
  fun before() {
    mockEngine = emptyMockEngine()
    syncApi = SyncApi(SERVER_URL, testHttpClient(mockEngine, ActualJson))
  }

  @AfterTest
  fun after() {
    mockEngine.close()
  }

  @Test
  fun `Fetch user files request headers`() = runTest {
    mockEngine.enqueue { respondJson(SYNC_LIST_BUDGETS_SUCCESS) }
    syncApi.fetchUserFiles(TOKEN)
    assertEquals(
      actual = mockEngine.latestRequestHeaders(),
      expected = mapOf(
        "X-ACTUAL-TOKEN" to listOf("abc-123"),
        "Accept" to listOf("application/json"),
        "Accept-Charset" to listOf("UTF-8"),
      ),
    )
  }

  @Test
  fun `Download user files request headers`() = runTest {
    mockEngine.enqueue { respondOk() }
    syncApi.downloadUserFile(TOKEN, BUDGET_ID)
    assertEquals(
      actual = mockEngine.latestRequestHeaders(),
      expected = mapOf(
        "X-ACTUAL-TOKEN" to listOf("abc-123"),
        "X-ACTUAL-FILE-ID" to listOf("xyz-789"),
        "Accept" to listOf("application/json"),
        "Accept-Charset" to listOf("UTF-8"),
      ),
    )
  }

  @Test
  fun `Fetch user file info request headers`() = runTest {
    mockEngine.enqueue { respondJson(SYNC_GET_USER_FILE_INFO_SUCCESS) }
    syncApi.fetchUserFileInfo(TOKEN, BUDGET_ID)
    assertEquals(
      actual = mockEngine.latestRequestHeaders(),
      expected = mapOf(
        "X-ACTUAL-TOKEN" to listOf("abc-123"),
        "X-ACTUAL-FILE-ID" to listOf("xyz-789"),
        "Accept" to listOf("application/json"),
        "Accept-Charset" to listOf("UTF-8"),
      ),
    )
  }

  @Test
  fun `Fetch user key request headers`() = runTest {
    mockEngine.enqueue { respondJson(SYNC_GET_USER_KEY_SUCCESS) }
    val body = GetUserKeyRequest(BUDGET_ID)
    syncApi.fetchUserKey(TOKEN, body)
    val request = mockEngine.requestHistory.last()
    assertEquals(
      actual = request.headers.toMap(),
      expected = mapOf(
        "X-ACTUAL-TOKEN" to listOf("abc-123"),
        "Accept" to listOf("application/json"),
        "Accept-Charset" to listOf("UTF-8"),
      ),
    )
    val actualBody = assertIs<TextContent>(request.body)
    assertEquals(actual = actualBody.text, expected = """{"fileId":"xyz-789"}""")
    assertEquals(actual = actualBody.contentType, expected = ContentType.Application.Json)
  }

  @Test
  fun `Fetch user files success response`() = runTest {
    // given
    mockEngine.enqueue { respondJson(SYNC_LIST_BUDGETS_SUCCESS) }

    // when
    val response = syncApi.fetchUserFiles(TOKEN)

    // then
    assertEquals(
      actual = response,
      expected = ListUserFilesResponse.Success(
        data = listOf(
          UserFile(
            deleted = 0,
            fileId = BudgetId("b328186c-c919-4333-959b-04e676c1ee46"),
            groupId = "afb25fc0-a294-4f71-ae8f-ce1e3a8fec10",
            name = "Main Budget",
            encryptKeyId = "2a66f4de-c530-4c06-8103-a48e26a0ce44",
            owner = null,
            encryptMeta = null,
            usersWithAccess = emptyList(),
          ),
          UserFile(
            deleted = 0,
            fileId = BudgetId("cf2b43ee-8067-48ed-ab5b-4e4e5531056e"),
            groupId = "ee90358a-f73e-4aa5-a922-653190fd31b7",
            name = "Test Budget",
            encryptKeyId = null,
            owner = "354d0cfe-cd36-44eb-a404-5990a3ab5c39",
            encryptMeta = null,
            usersWithAccess = listOf(
              UserWithAccess(
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
  fun `Fetch user files failure response`() = runTest {
    // given
    mockEngine.enqueue { respondJson(SYNC_LIST_BUDGETS_FAILURE, HttpStatusCode.BadRequest) }

    // when
    try {
      syncApi.fetchUserFiles(TOKEN)
      fail("Should have thrown!")
    } catch (e: ClientRequestException) {
      // Then
      val body = e.response.body<ListUserFilesResponse.Failure>()
      assertEquals(actual = body, expected = ListUserFilesResponse.Failure("Something broke"))
    }
  }

  @Test
  fun `Get user key success`() = runTest {
    // given
    mockEngine.enqueue { respondJson(SYNC_GET_USER_KEY_SUCCESS) }

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

  @Test
  fun `Get user key failure`() = runTest {
    // given
    mockEngine.enqueue { respondJson(SYNC_GET_USER_KEY_FAILURE, HttpStatusCode.Unauthorized) }

    // when
    val response = try {
      val body = GetUserKeyRequest(BUDGET_ID)
      syncApi.fetchUserKey(TOKEN, body)
      fail("Should have thrown!")
    } catch (e: ClientRequestException) {
      e.response.body<GetUserKeyResponse.Failure>()
    }

    // then
    assertEquals(
      actual = response,
      expected = GetUserKeyResponse.Failure(
        reason = FailureReason.Known.Unauthorized,
        details = "token-not-found",
      ),
    )
  }

  @Test
  fun `Get file info success`() = runTest {
    // given
    mockEngine.enqueue { respondJson(SYNC_GET_USER_FILE_INFO_SUCCESS) }

    // when
    val response = syncApi.fetchUserFileInfo(TOKEN, BUDGET_ID)

    // then
    assertEquals(
      actual = response,
      expected = GetUserFileInfoResponse.Success(
        data = UserFile(
          deleted = 0,
          fileId = BudgetId("b328186c-c819-4333-959b-04f676c1ee46"),
          groupId = "afb25fc0-b294-4f71-ae8f-ce1e4a8fec10",
          name = "Main Budget",
          encryptKeyId = null,
          owner = null,
          usersWithAccess = emptyList(),
          encryptMeta = EncryptMeta(
            keyId = "2a66f5de-c530-4c06-8103-a48f26a0ce44",
            algorithm = "aes-256-gcm",
            iv = "7tzgaLCrSFxVfzZR",
            authTag = "25nafe0UpzehRCks/xQjoB==",
          ),
        ),
      ),
    )
  }

  @Test
  fun `Get file info file not found failure`() = runTest {
    // given
    mockEngine.enqueue { respondJson(SYNC_GET_USER_FILE_INFO_FILE_NOT_FOUND, HttpStatusCode.BadRequest) }

    // when
    val response = try {
      syncApi.fetchUserFileInfo(TOKEN, BUDGET_ID)
      fail("Should have thrown!")
    } catch (e: ClientRequestException) {
      e.response.body<GetUserFileInfoResponse.Failure>()
    }

    // then
    assertEquals(
      actual = response,
      expected = GetUserFileInfoResponse.Failure(FailureReason.Known.FileNotFound, details = null),
    )
  }

  @Test
  fun `Get file info unauthorised failure`() = runTest {
    // given
    mockEngine.enqueue { respondJson(SYNC_GET_USER_FILE_INFO_UNAUTHORISED, HttpStatusCode.Unauthorized) }

    // when
    val response = try {
      syncApi.fetchUserFileInfo(TOKEN, BUDGET_ID)
      fail("Should have thrown!")
    } catch (e: ClientRequestException) {
      e.response.body<GetUserFileInfoResponse.Failure>()
    }

    // then
    assertEquals(
      actual = response,
      expected = GetUserFileInfoResponse.Failure(FailureReason.Known.Unauthorized, details = "token-not-found"),
    )
  }
}
