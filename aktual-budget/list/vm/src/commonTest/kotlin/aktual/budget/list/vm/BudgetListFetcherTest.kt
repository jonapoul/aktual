package aktual.budget.list.vm

import aktual.api.client.AktualApis
import aktual.api.client.AktualApisStateHolder
import aktual.api.client.AktualJson
import aktual.api.client.SyncApi
import aktual.budget.model.Budget
import aktual.budget.model.BudgetId
import aktual.budget.model.BudgetState
import aktual.core.model.Protocol
import aktual.core.model.ServerUrl
import aktual.core.model.Token
import aktual.core.prefs.KeyPreferences
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
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import java.net.NoRouteToHostException
import kotlin.test.AfterTest
import kotlin.test.Test

class BudgetListFetcherTest {
  private lateinit var budgetListFetcher: BudgetListFetcher
  private lateinit var apisStateHolder: AktualApisStateHolder
  private lateinit var mockEngine: MockEngine.Queue
  private lateinit var keyPreferences: KeyPreferences

  private fun TestScope.before() {
    mockEngine = emptyMockEngine()
    apisStateHolder = AktualApisStateHolder()
    keyPreferences = mockk { every { contains(any()) } returns false }
    budgetListFetcher = BudgetListFetcher(
      contexts = TestCoroutineContexts(standardDispatcher),
      apisStateHolder = apisStateHolder,
      keyPreferences = keyPreferences,
    )
  }

  @AfterTest
  fun after() {
    mockEngine.close()
  }

  @Test
  fun `Failure if no APIs stored`() = runTest {
    before()
    apisStateHolder.update { null }
    val result = budgetListFetcher.fetchBudgets(TOKEN)
    assertThat(result).isEqualTo(FetchBudgetsResult.NotLoggedIn)
  }

  @Test
  fun `Handle success response`() = runTest {
    before()

    // given
    mockEngine += { respondJson(VALID_RESPONSE) }
    apisStateHolder.update { buildApis() }

    // when
    val result = budgetListFetcher.fetchBudgets(TOKEN)

    // then
    assertThat(result).isEqualTo(
      FetchBudgetsResult.Success(
        budgets = listOf(
          Budget(
            name = "Main Budget",
            state = BudgetState.Unknown,
            encryptKeyId = "7fe20d96-ab62-43bc-b69c-53f55a26cbbf",
            groupId = "16f9c400-cdf5-43ae-983f-4dbcccb10ccf",
            cloudFileId = BudgetId("525fecc4-5080-4d01-b2ea-6032e5ee25c1"),
            hasKey = false,
          ),
        ),
      ),
    )
  }

  @Test
  fun `Handle invalid JSON format`() = runTest {
    before()

    // given
    val body = """
      {
        "status": "ok",
        "data": [ { "invalid_format": true } ]
      }
    """.trimIndent()
    mockEngine += { respondJson(body) }
    apisStateHolder.update { buildApis() }

    // when
    val result = budgetListFetcher.fetchBudgets(TOKEN)

    // then
    assertThat(result).isInstanceOf<FetchBudgetsResult.InvalidResponse>()
  }

  @Test
  fun `Handle network failure`() = runTest {
    before()

    // given
    val syncApi = mockk<SyncApi> {
      coEvery { fetchUserFiles(TOKEN) } throws NoRouteToHostException()
    }
    mockEngine += { respondJson(VALID_RESPONSE) }
    apisStateHolder.update { buildApis(syncApi) }

    // when
    val result = budgetListFetcher.fetchBudgets(TOKEN)

    // then
    assertThat(result).isInstanceOf<FetchBudgetsResult.NetworkFailure>()
  }

  @Test
  fun `Handle failure response`() = runTest {
    before()

    // given
    val responseJson = """
      {
        "status": "error",
        "reason": "something broke"
      }
    """.trimIndent()
    mockEngine += { respondJson(responseJson, HttpStatusCode.Forbidden) }
    apisStateHolder.update { buildApis() }

    // when
    val result = budgetListFetcher.fetchBudgets(TOKEN)

    // then
    assertThat(result).isEqualTo(
      FetchBudgetsResult.FailureResponse(reason = "something broke"),
    )
  }

  private fun buildApis(
    syncApi: SyncApi = SyncApi(SERVER_URL, testHttpClient(mockEngine, AktualJson)),
  ) = mockk<AktualApis> { every { sync } returns syncApi }

  private companion object {
    val TOKEN = Token(value = "abc-123")
    val SERVER_URL = ServerUrl(Protocol.Https, "test.unused.com")

    val VALID_RESPONSE = """
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
    """.trimIndent()
  }
}
