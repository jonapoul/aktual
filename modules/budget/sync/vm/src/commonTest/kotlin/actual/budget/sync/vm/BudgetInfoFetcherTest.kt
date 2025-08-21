package actual.budget.sync.vm

import actual.core.model.LoginToken
import actual.api.client.ActualApis
import actual.api.client.ActualApisStateHolder
import actual.api.client.ActualJson
import actual.api.client.SyncApi
import actual.api.model.sync.EncryptMeta
import actual.api.model.sync.UserFile
import actual.budget.model.BudgetId
import actual.budget.sync.vm.BudgetInfoFetcher.Result
import actual.core.model.KeyId
import actual.core.model.ServerUrl
import actual.core.model.base64
import actual.test.emptyMockEngine
import actual.test.plusAssign
import actual.test.respondJson
import actual.test.testHttpClient
import alakazam.test.core.TestCoroutineContexts
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.http.HttpStatusCode
import io.mockk.mockk
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import java.net.NoRouteToHostException
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertIs

class BudgetInfoFetcherTest {
  private lateinit var budgetInfoFetcher: BudgetInfoFetcher
  private lateinit var apisStateHolder: ActualApisStateHolder
  private lateinit var mockEngine: MockEngine
  private lateinit var client: HttpClient
  private lateinit var syncApi: SyncApi

  @AfterTest
  fun after() {
    mockEngine.close()
  }

  private fun TestScope.before() {
    mockEngine = emptyMockEngine()
    client = testHttpClient(mockEngine, ActualJson)
    syncApi = SyncApi(SERVER_URL, client)
    apisStateHolder = ActualApisStateHolder()
    apisStateHolder.update { actualApis() }
    budgetInfoFetcher = BudgetInfoFetcher(
      contexts = TestCoroutineContexts(StandardTestDispatcher(testScheduler)),
      apisStateHolder = apisStateHolder,
    )
  }

  @Test
  fun `Handle success`() = runTest {
    // given
    before()
    val response = """
      {
          "status": "ok",
          "data": {
              "deleted": 0,
              "fileId": "b328186c-c819-4333-959b-04f676c1ee46",
              "groupId": "afb25fc0-b294-4f71-ae8f-ce1e4a8fec10",
              "name": "Main Budget",
              "encryptMeta": {
                  "keyId": "2a66f5de-c530-4c06-8103-a48f26a0ce44",
                  "algorithm": "aes-256-gcm",
                  "iv": "7tzgaLCrSFxVfzZR",
                  "authTag": "25nafe0UpzehRCks/xQjoB=="
              },
              "usersWithAccess": []
          }
      }
    """.trimIndent()
    mockEngine += { respondJson(response) }

    // then
    val userFile = UserFile(
      deleted = 0,
      fileId = BudgetId("b328186c-c819-4333-959b-04f676c1ee46"),
      groupId = "afb25fc0-b294-4f71-ae8f-ce1e4a8fec10",
      name = "Main Budget",
      encryptKeyId = null,
      owner = null,
      usersWithAccess = emptyList(),
      encryptMeta = EncryptMeta(
        keyId = KeyId("2a66f5de-c530-4c06-8103-a48f26a0ce44"),
        algorithm = "aes-256-gcm",
        iv = "7tzgaLCrSFxVfzZR".base64,
        authTag = "25nafe0UpzehRCks/xQjoB==".base64,
      ),
    )
    assertEquals(actual = budgetInfoFetcher.fetch(TOKEN, BUDGET_ID), expected = Result.Success(userFile))
  }

  @Test
  fun `Not logged in if no API client cached`() = runTest {
    // given
    before()
    apisStateHolder.reset()

    // then
    assertEquals(
      actual = budgetInfoFetcher.fetch(TOKEN, BUDGET_ID),
      expected = Result.NotLoggedIn,
    )
  }

  @Test
  fun `Handle HTTP failure`() = runTest {
    // given
    before()
    mockEngine += {
      respondJson(
        status = HttpStatusCode.Unauthorized,
        content = """{ "status": "error", "reason": "unauthorized", "details": "token-not-found" }""",
      )
    }

    // then
    assertEquals(
      actual = budgetInfoFetcher.fetch(TOKEN, BUDGET_ID),
      expected = Result.HttpFailure("unauthorized"),
    )
  }

  @Test
  fun `Handle unexpected HTTP failure body`() = runTest {
    // given
    before()
    mockEngine += {
      respondJson(
        status = HttpStatusCode.Unauthorized,
        content = """{ "unexpected-key": 123 }""",
      )
    }

    // when
    val result = budgetInfoFetcher.fetch(TOKEN, BUDGET_ID)

    // then
    assertIs<Result.HttpFailure>(result)
    assertContains(result.reason, "failed parsing")
  }

  @Test
  fun `Handle network error`() = runTest {
    // given
    before()
    mockEngine += { throw NoRouteToHostException() }

    // then
    assertIs<Result.IOFailure>(budgetInfoFetcher.fetch(TOKEN, BUDGET_ID))
  }

  private fun actualApis() = ActualApis(
    serverUrl = SERVER_URL,
    client = client,
    account = mockk(),
    base = mockk(),
    health = mockk(),
    metrics = mockk(),
    sync = syncApi,
    syncDownload = mockk(),
  )

  private companion object {
    val TOKEN = LoginToken("abc-123")
    val BUDGET_ID = BudgetId("xyz-789")
    val SERVER_URL = ServerUrl("https://website.com")
  }
}
