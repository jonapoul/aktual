package aktual.account.domain

import aktual.api.client.AccountApi
import aktual.api.client.AktualApis
import aktual.api.client.AktualApisStateHolder
import aktual.api.model.account.LoginRequest
import aktual.core.connection.ConnectionMonitor
import aktual.core.model.Password
import aktual.core.model.Protocol
import aktual.core.model.ServerUrl
import aktual.core.model.Token
import aktual.core.prefs.AppGlobalPreferences
import aktual.core.prefs.AppGlobalPreferencesImpl
import aktual.test.TestClientFactory
import aktual.test.assertThatNextEmission
import aktual.test.buildPreferences
import aktual.test.emptyMockEngine
import aktual.test.respondJson
import alakazam.test.TestCoroutineContexts
import alakazam.test.unconfinedDispatcher
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNull
import io.ktor.client.engine.mock.MockEngine
import io.ktor.http.HttpStatusCode
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import java.io.IOException
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import okio.FileSystem
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class LoginRequesterTest {
  private lateinit var loginRequester: LoginRequester
  private lateinit var apisStateHolder: AktualApisStateHolder
  private lateinit var preferences: AppGlobalPreferences
  private lateinit var connectionMonitor: ConnectionMonitor
  private lateinit var mockEngine: MockEngine.Queue
  private lateinit var fileSystem: FileSystem

  @AfterTest
  fun after() {
    mockEngine.close()
  }

  private fun TestScope.before() {
    val dispatcher = unconfinedDispatcher
    val flowPrefs = buildPreferences(dispatcher)
    preferences = AppGlobalPreferencesImpl(flowPrefs)
    apisStateHolder = AktualApisStateHolder()
    mockEngine = emptyMockEngine()
    fileSystem = FileSystem.SYSTEM

    connectionMonitor =
      ConnectionMonitor(
        scope = backgroundScope,
        contexts = TestCoroutineContexts(dispatcher),
        clientFactory = TestClientFactory(mockEngine),
        apiStateHolder = apisStateHolder,
        preferences = preferences,
        fileSystem = fileSystem,
      )

    loginRequester =
      LoginRequester(
        contexts = TestCoroutineContexts(dispatcher),
        apisStateHolder = apisStateHolder,
        preferences = preferences,
      )
  }

  @Test
  fun `Successful call`() = runTest {
    before()

    // Given a URL is set
    preferences.serverUrl.set(EXAMPLE_URL)

    // and we have a non-null api
    connectionMonitor.start()
    apisStateHolder.filterNotNull().first()

    preferences.token.asFlow().test {
      assertThatNextEmission().isNull()

      // When we log in with a successful response
      val body = """{ "status": "ok", "data": { "token": "$EXAMPLE_TOKEN" } } """
      mockEngine += { respondJson(body) }
      val result = loginRequester.logIn(EXAMPLE_PASSWORD)

      // Then a success response was parsed, and the token was stored in prefs
      assertThat(result).isInstanceOf<LoginResult.Success>()
      assertThatNextEmission().isEqualTo(EXAMPLE_TOKEN)
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Network error`() = runTest {
    before()

    // Given a mock API is provided, which throws a network error when called
    val errorMessage = "something broke"
    val accountApi = mockk<AccountApi>()
    coEvery { accountApi.login(any<LoginRequest.Password>()) } throws IOException(errorMessage)
    val apis = mockk<AktualApis>()
    every { apis.account } returns accountApi
    apisStateHolder.update { apis }

    // When we log in
    val result = loginRequester.logIn(EXAMPLE_PASSWORD)

    // Then we get a failure result
    assertThat(result).isEqualTo(LoginResult.NetworkFailure(errorMessage))
  }

  @Test
  fun `Invalid password`() = runTest {
    before()

    // Given a URL is set
    preferences.serverUrl.set(EXAMPLE_URL)

    // and we have a non-null api
    connectionMonitor.start()
    apisStateHolder.filterNotNull().first()

    // When we log in with a successful response, but a null token
    val body =
      """
      {
        "status": "ok",
        "data": { "token": null }
      }
      """
        .trimIndent()
    mockEngine += { respondJson(body) }
    val result = loginRequester.logIn(EXAMPLE_PASSWORD)

    // Then a success result was parsed, and the token was stored in prefs
    assertThat(result).isEqualTo(LoginResult.InvalidPassword)
  }

  @Test
  fun `HTTP failure`() = runTest {
    before()

    // Given a URL is set
    preferences.serverUrl.set(EXAMPLE_URL)

    // and we have a non-null api
    connectionMonitor.start()
    apisStateHolder.filterNotNull().first()

    // When we log in with a failed response
    val body =
      """
      {
        "status": "error",
        "reason": "Some error"
      }
      """
        .trimIndent()
    mockEngine += { respondJson(body, HttpStatusCode.InternalServerError) }
    val result = loginRequester.logIn(EXAMPLE_PASSWORD)

    // Then a HTTP result
    assertThat(result).isInstanceOf<LoginResult.HttpFailure>()
  }

  private companion object {
    val EXAMPLE_PASSWORD = Password(value = "P@ssw0rd")
    val EXAMPLE_TOKEN = Token(value = "abc123")
    val EXAMPLE_URL = ServerUrl(Protocol.Https, "website.com")
  }
}
