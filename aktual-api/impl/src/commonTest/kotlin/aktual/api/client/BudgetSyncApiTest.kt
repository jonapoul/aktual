package aktual.api.client

import aktual.api.model.account.FailureReason
import aktual.core.model.AktualJson
import aktual.core.model.BudgetServer
import aktual.test.SyncResponses
import aktual.test.TestBudgetLocalPreferences
import aktual.test.emptyMockEngine
import aktual.test.latestRequestHeaders
import aktual.test.latestRequestUrl
import aktual.test.respondJson
import aktual.test.testHttpClient
import assertk.all
import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.doesNotContainKey
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isTrue
import assertk.assertions.key
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
        prop(SyncRejectedException::reason).isEqualTo(ClockDrift)
        prop(SyncRejectedException::isClockDrift).isTrue()
      }
  }

  @Test
  fun `File not found rejection`() = runTest {
    mockEngine += { respond(content = "file-not-found", status = BadRequest) }

    assertFailure { api.syncBudget(EMPTY) }
      .isInstanceOf<SyncRejectedException>()
      .all {
        prop(SyncRejectedException::reason).isEqualTo(FileNotFound)
        prop(SyncRejectedException::isFileNotFound).isTrue()
        prop(SyncRejectedException::isClockDrift).isFalse()
      }
  }

  @Test
  fun `Invalid timestamp rejection`() = runTest {
    mockEngine += { respond(content = "invalid-timestamp", status = BadRequest) }

    assertFailure { api.syncBudget(EMPTY) }
      .isInstanceOf<SyncRejectedException>()
      .all {
        prop(SyncRejectedException::reason).isEqualTo(FailureReason("invalid-timestamp"))
        prop(SyncRejectedException::isClockDrift).isFalse()
        prop(SyncRejectedException::isFileNotFound).isFalse()
      }
  }

  @Test
  fun `Other client errors are not rejections`() = runTest {
    mockEngine += { respond(content = "", status = Unauthorized) }

    assertFailure { api.syncBudget(EMPTY) }.isInstanceOf<ClientRequestException>()
  }

  @Test
  fun `Upload sends the zip with the file details`() = runTest {
    mockEngine += { respondJson(SyncResponses.UPLOAD_USER_FILE_SUCCESS_200) }

    val groupId = api.uploadBudget(BUDGET_ID, "My Budget", groupId = null, zip = byteArrayOf(1, 2))

    assertThat(groupId).isEqualTo("7f8b1c2e-3d4a-4b5c-9e6f-0a1b2c3d4e5f")
    assertThat(mockEngine.latestRequestUrl())
      .isEqualTo("https://test.server.com/sync/upload-user-file")
    assertThat(mockEngine.latestRequestHeaders()).all {
      key("X-ACTUAL-TOKEN").containsExactly(TOKEN.value)
      key("X-ACTUAL-FILE-ID").containsExactly(BUDGET_ID.value)
      key("X-ACTUAL-NAME").containsExactly("My%20Budget")
      key("X-ACTUAL-FORMAT").containsExactly("2")
      doesNotContainKey("X-ACTUAL-GROUP-ID")
    }
  }

  @Test
  fun `Upload sends an existing group ID`() = runTest {
    mockEngine += { respondJson(SyncResponses.UPLOAD_USER_FILE_SUCCESS_200) }

    api.uploadBudget(BUDGET_ID, "My Budget", groupId = "group", zip = byteArrayOf())

    assertThat(mockEngine.latestRequestHeaders()).key("X-ACTUAL-GROUP-ID").containsExactly("group")
  }

  @Test
  fun `Upload rejection`() = runTest {
    mockEngine += { respond(content = "file-has-new-key", status = BadRequest) }

    assertFailure { api.uploadBudget(BUDGET_ID, "My Budget", groupId = null, zip = byteArrayOf()) }
      .isInstanceOf<UploadRejectedException>()
      .prop(UploadRejectedException::reason)
      .isEqualTo(FailureReason("file-has-new-key"))
  }
}
