package aktual.api.client

import aktual.api.model.account.LoginRequest
import aktual.core.model.AktualJson
import aktual.core.model.Password
import aktual.test.AccountResponses
import aktual.test.emptyMockEngine
import aktual.test.latestRequest
import aktual.test.respondJson
import aktual.test.testHttpClient
import assertk.assertThat
import assertk.assertions.isEqualTo
import io.ktor.client.engine.mock.MockEngine
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class AccountApiTest {
  private lateinit var mockEngine: MockEngine.Queue
  private lateinit var api: AccountApi

  @BeforeTest
  fun before() {
    mockEngine = emptyMockEngine()
    api = AccountApiImpl(client = testHttpClient(mockEngine, AktualJson), serverUrl = SERVER_URL)
  }

  @AfterTest
  fun after() {
    mockEngine.close()
  }

  @Test
  fun `Header login sends raw password`() = runTest {
    mockEngine += { respondJson(AccountResponses.LOGIN_SUCCESS_200) }

    api.login(LoginRequest.Header(), Password("P@ssw0rd!"))

    val request = mockEngine.latestRequest()
    assertThat(request.headers[AktualHeaders.PASSWORD]).isEqualTo("P@ssw0rd!")
  }
}
