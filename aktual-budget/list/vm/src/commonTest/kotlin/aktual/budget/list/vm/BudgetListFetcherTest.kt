package aktual.budget.list.vm

import aktual.api.client.SyncApi
import aktual.api.client.SyncApiImpl
import aktual.api.model.account.FailureReason
import aktual.api.model.sync.UserFile
import aktual.budget.model.BudgetId
import aktual.core.model.KeyId
import aktual.core.model.Protocol
import aktual.core.model.ServerUrl
import aktual.core.model.Token
import aktual.test.emptyMockEngine
import aktual.test.respondJson
import aktual.test.testHttpClient
import alakazam.test.TestCoroutineContexts
import alakazam.test.standardDispatcher
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import io.ktor.client.engine.mock.MockEngine
import io.ktor.http.HttpStatusCode
import io.mockk.coEvery
import io.mockk.mockk
import java.net.NoRouteToHostException
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import okio.FileSystem

class BudgetListFetcherTest {
  private lateinit var budgetListFetcher: BudgetListFetcher
  private lateinit var mockEngine: MockEngine.Queue

  @BeforeTest
  fun before() {
    mockEngine = emptyMockEngine()
  }

  private fun TestScope.before(
    syncApi: SyncApi = SyncApiImpl(testHttpClient(mockEngine), FileSystem.SYSTEM, SERVER_URL)
  ) {
    budgetListFetcher =
      BudgetListFetcher(syncApi = syncApi, contexts = TestCoroutineContexts(standardDispatcher))
  }

  @AfterTest
  fun after() {
    mockEngine.close()
  }

  @Test
  fun `Handle success response`() = runTest {
    // given
    mockEngine += { respondJson(VALID_RESPONSE) }
    before()

    // when
    val result = budgetListFetcher.fetchBudgets(TOKEN)

    // then
    assertThat(result)
      .isEqualTo(
        FetchBudgetsResult.Success(
          userFiles =
            persistentListOf(
              UserFile(
                deleted = 0,
                fileId = BudgetId("525fecc4-5080-4d01-b2ea-6032e5ee25c1"),
                groupId = "16f9c400-cdf5-43ae-983f-4dbcccb10ccf",
                name = "Main Budget",
                encryptKeyId = KeyId("7fe20d96-ab62-43bc-b69c-53f55a26cbbf"),
                owner = null,
              )
            )
        )
      )
  }

  @Test
  fun `Handle invalid JSON format`() = runTest {
    // given
    val body =
      """
      {
        "status": "ok",
        "data": [ { "invalid_format": true } ]
      }
      """
        .trimIndent()
    mockEngine += { respondJson(body) }
    before()

    // when
    val result = budgetListFetcher.fetchBudgets(TOKEN)

    // then
    assertThat(result).isInstanceOf<FetchBudgetsResult.InvalidResponse>()
  }

  @Test
  fun `Handle network failure`() = runTest {
    // given
    val syncApi =
      mockk<SyncApi> { coEvery { fetchUserFiles(TOKEN) } throws NoRouteToHostException() }
    before(syncApi)

    // when
    val result = budgetListFetcher.fetchBudgets(TOKEN)

    // then
    assertThat(result).isInstanceOf<FetchBudgetsResult.NetworkFailure>()
  }

  @Test
  fun `Handle failure response`() = runTest {
    // given
    val responseJson =
      """
      {
        "status": "error",
        "reason": "something broke"
      }
      """
        .trimIndent()
    mockEngine += { respondJson(responseJson, HttpStatusCode.Forbidden) }
    before()

    // when
    val result = budgetListFetcher.fetchBudgets(TOKEN)

    // then
    assertThat(result)
      .isEqualTo(FetchBudgetsResult.FailureResponse(FailureReason("something broke")))
  }

  private companion object {
    val TOKEN = Token(value = "abc-123")
    val SERVER_URL = ServerUrl(Protocol.Https, "test.unused.com")

    val VALID_RESPONSE =
      """
      {
        "status": "ok",
        "data": [
          {
            "deleted": 0,
            "fileId": "525fecc4-5080-4d01-b2ea-6032e5ee25c1",
            "groupId": "16f9c400-cdf5-43ae-983f-4dbcccb10ccf",
            "name": "Main Budget",
            "encryptKeyId": "7fe20d96-ab62-43bc-b69c-53f55a26cbbf",
            "owner": null,
            "usersWithAccess": []
          }
        ]
      }
      """
        .trimIndent()
  }
}
