package actual.budget.list.vm

import actual.api.client.ActualApis
import actual.api.client.ActualApisStateHolder
import actual.api.client.SyncApi
import actual.log.EmptyLogger
import actual.login.model.LoginToken
import actual.test.MockWebServerRule
import actual.test.TestCoroutineContexts
import alakazam.test.core.standardDispatcher
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.net.NoRouteToHostException
import kotlin.test.assertEquals
import kotlin.test.assertIs

class BudgetListFetcherTest {
  @get:Rule
  val webServerRule = MockWebServerRule()

  private lateinit var budgetListFetcher: BudgetListFetcher
  private lateinit var apisStateHolder: ActualApisStateHolder

  private fun TestScope.before() {
    apisStateHolder = ActualApisStateHolder()
    budgetListFetcher = BudgetListFetcher(
      contexts = TestCoroutineContexts(standardDispatcher),
      apisStateHolder = apisStateHolder,
      logger = EmptyLogger,
    )
  }

  @Test
  fun `Failure if no APIs stored`() = runTest {
    before()
    apisStateHolder.update { null }
    val result = budgetListFetcher.fetchBudgets(TOKEN)
    assertEquals(actual = result, expected = FetchBudgetsResult.NotLoggedIn)
  }

  @Test
  fun `Handle success response`() = runTest {
    before()

    // given
    apisStateHolder.update { buildApis() }
    webServerRule.enqueue(code = 200, body = VALID_RESPONSE)

    // when
    val result = budgetListFetcher.fetchBudgets(TOKEN)

    // then
    assertEquals(
      actual = result,
      expected = FetchBudgetsResult.Success(
        budgets = listOf(
          Budget(
            name = "Main Budget",
            state = BudgetState.Unknown,
            encryptKeyId = "7fe20d96-ab62-43bc-b69c-53f55a26cbbf",
            groupId = "16f9c400-cdf5-43ae-983f-4dbcccb10ccf",
            cloudFileId = "525fecc4-5080-4d01-b2ea-6032e5ee25c1",
          ),
        ),
      ),
    )
  }

  @Test
  fun `Handle invalid JSON format`() = runTest {
    before()

    // given
    apisStateHolder.update { buildApis() }
    webServerRule.enqueue(
      """
        {
          "status": "ok",
          "data": [ { "invalid_format": true } ]
        }
      """.trimIndent(),
    )

    // when
    val result = budgetListFetcher.fetchBudgets(TOKEN)

    // then
    assertIs<FetchBudgetsResult.InvalidResponse>(result)
  }

  @Test
  fun `Handle network failure`() = runTest {
    before()

    // given
    val syncApi = mockk<SyncApi> {
      coEvery { listUserFiles(TOKEN) } throws NoRouteToHostException()
    }
    apisStateHolder.update { buildApis(syncApi) }
    webServerRule.enqueue(VALID_RESPONSE, code = 200)

    // when
    val result = budgetListFetcher.fetchBudgets(TOKEN)

    // then
    assertIs<FetchBudgetsResult.NetworkFailure>(result)
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
    apisStateHolder.update { buildApis() }
    webServerRule.enqueue(responseJson, code = 403)

    // when
    val result = budgetListFetcher.fetchBudgets(TOKEN)

    // then
    assertEquals(
      expected = FetchBudgetsResult.FailureResponse(reason = "something broke"),
      actual = result,
    )
  }

  private fun buildApis(
    syncApi: SyncApi = webServerRule.buildApi(),
  ) = mockk<ActualApis> { every { sync } returns syncApi }

  private companion object {
    val TOKEN = LoginToken(value = "abc-123")

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
