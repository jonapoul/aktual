package aktual.api.client

import aktual.api.model.banksync.BankSyncSecret.SimpleFinAccessKey
import aktual.api.model.banksync.BankSyncSecret.SimpleFinToken
import aktual.api.model.banksync.SecretResponse
import aktual.core.model.AktualJson
import aktual.core.model.BudgetServer
import aktual.test.SecretResponses
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
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class SecretsApiTest {
  private lateinit var mockEngine: MockEngine.Queue
  private lateinit var api: SecretsApi

  @BeforeTest
  fun before() {
    mockEngine = emptyMockEngine()
    api =
      SecretsApiImpl(
        client = testHttpClient(mockEngine, AktualJson),
        server = BudgetServer.Remote(SERVER_URL, TOKEN),
      )
  }

  @AfterTest
  fun after() {
    mockEngine.close()
  }

  @Test
  fun `Set request`() = runTest {
    mockEngine += { respondJson(SecretResponses.SET_SUCCESS_200) }

    val response = api.set(SimpleFinToken, "abc")

    val request = mockEngine.latestRequest()
    assertThat(request.method).isEqualTo(HttpMethod.Post)
    assertThat(request.headers[AktualHeaders.TOKEN]).isEqualTo(TOKEN.value)
    assertThat(mockEngine.latestRequestUrl()).isEqualTo("https://test.server.com/secret")
    assertThat(request.body)
      .isInstanceOf<TextContent>()
      .prop(TextContent::text)
      .isEqualTo("""{"name":"simplefin_token","value":"abc"}""")
    assertThat(response).isEqualTo(Success)
  }

  @Test
  fun `Clearing deletes the secret`() = runTest {
    mockEngine += { respondJson(SecretResponses.SET_SUCCESS_200) }

    val response = api.set(SimpleFinAccessKey, value = null)

    assertThat(mockEngine.latestRequest().method).isEqualTo(HttpMethod.Delete)
    assertThat(mockEngine.latestRequestUrl())
      .isEqualTo("https://test.server.com/secret/simplefin_accessKey")
    assertThat(response).isEqualTo(Success)
  }

  @Test
  fun `Parse refusal`() = runTest {
    mockEngine += {
      respondJson(SecretResponses.SET_NOT_ADMIN_403, status = HttpStatusCode.Forbidden)
    }

    val response = api.set(SimpleFinToken, "abc")

    assertThat(response)
      .isEqualTo(
        SecretResponse.Failed(
          reason = SecretResponse.NOT_ADMIN,
          details = "You have to be admin to manage global secrets",
        )
      )
  }
}
