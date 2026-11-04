package aktual.api.client

import aktual.core.model.AktualJson
import aktual.core.model.BudgetServer
import aktual.test.TestBudgetLocalPreferences
import aktual.test.emptyMockEngine
import aktual.test.testHttpClient
import assertk.all
import assertk.assertFailure
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isTrue
import assertk.assertions.prop
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.ClientRequestException
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.fail
import kotlinx.coroutines.test.runTest

class BudgetSyncApiTest {
  private lateinit var mockEngine: MockEngine.Queue
  private lateinit var api: BudgetSyncApi

  @BeforeTest
  fun before() {
    mockEngine = emptyMockEngine()
    api =
      BudgetSyncApiImpl(
        client = testHttpClient(mockEngine, AktualJson),
        server = BudgetServer.Remote(SERVER_URL, TOKEN),
        prefs = TestBudgetLocalPreferences(),
        decoder = { _, _ -> fail("Should not decode a failed response") },
      )
  }

  @AfterTest
  fun after() {
    mockEngine.close()
  }

  @Test
  fun `Clock drift rejection`() = runTest {
    mockEngine += { respond(content = "clock-drift", status = BadRequest) }

    assertFailure { api.syncBudget(EMPTY) }
      .isInstanceOf<SyncRejectedException>()
      .all {
        prop(SyncRejectedException::reason).isEqualTo("clock-drift")
        prop(SyncRejectedException::isClockDrift).isTrue()
      }
  }

  @Test
  fun `Invalid timestamp rejection`() = runTest {
    mockEngine += { respond(content = "invalid-timestamp", status = BadRequest) }

    assertFailure { api.syncBudget(EMPTY) }
      .isInstanceOf<SyncRejectedException>()
      .all {
        prop(SyncRejectedException::reason).isEqualTo("invalid-timestamp")
        prop(SyncRejectedException::isClockDrift).isFalse()
      }
  }

  @Test
  fun `Other client errors are not rejections`() = runTest {
    mockEngine += { respond(content = "", status = Unauthorized) }

    assertFailure { api.syncBudget(EMPTY) }.isInstanceOf<ClientRequestException>()
  }
}
