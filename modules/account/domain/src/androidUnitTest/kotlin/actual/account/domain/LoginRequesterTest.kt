package actual.account.domain

import actual.api.client.AccountApi
import actual.api.client.ActualApis
import actual.api.client.ActualApisStateHolder
import actual.api.model.account.LoginRequest
import actual.core.connection.ConnectionMonitor
import actual.core.model.LoginToken
import actual.core.model.Password
import actual.core.model.Protocol
import actual.core.model.ServerUrl
import actual.prefs.AppGlobalPreferences
import actual.test.TestClientFactory
import actual.test.buildPreferences
import actual.test.emptyMockEngine
import actual.test.respondJson
import alakazam.test.core.TestCoroutineContexts
import alakazam.test.core.unconfinedDispatcher
import app.cash.turbine.test
import io.ktor.client.engine.mock.MockEngine
import io.ktor.http.HttpStatusCode
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import okio.FileSystem
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.IOException
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

@RunWith(RobolectricTestRunner::class)
internal class LoginRequesterTest {

  private lateinit var loginRequester: LoginRequester
  private lateinit var apisStateHolder: ActualApisStateHolder
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
    preferences = AppGlobalPreferences(flowPrefs)
    apisStateHolder = ActualApisStateHolder()
    mockEngine = emptyMockEngine()
    fileSystem = FileSystem.SYSTEM

    connectionMonitor = ConnectionMonitor(
      scope = backgroundScope,
      clientFactory = TestClientFactory(mockEngine),
      apiStateHolder = apisStateHolder,
      preferences = preferences,
      fileSystem = fileSystem,
    )

    loginRequester = LoginRequester(
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

    preferences.loginToken.asFlow().test {
      assertNull(awaitItem())

      // When we log in with a successful response
      val body = """{ "status": "ok", "data": { "token": "$EXAMPLE_TOKEN" } } """
      mockEngine += { respondJson(body) }
      val result = loginRequester.logIn(EXAMPLE_PASSWORD)

      // Then a success response was parsed, and the token was stored in prefs
      assertIs<LoginResult.Success>(result)
      assertEquals(expected = EXAMPLE_TOKEN, actual = awaitItem())
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
    val apis = mockk<ActualApis>()
    every { apis.account } returns accountApi
    apisStateHolder.update { apis }

    // When we log in
    val result = loginRequester.logIn(EXAMPLE_PASSWORD)

    // Then we get a failure result
    assertEquals(expected = LoginResult.NetworkFailure(errorMessage), actual = result)
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
    val body = """
      {
        "status": "ok",
        "data": { "token": null }
      }
    """.trimIndent()
    mockEngine += { respondJson(body) }
    val result = loginRequester.logIn(EXAMPLE_PASSWORD)

    // Then a success result was parsed, and the token was stored in prefs
    assertEquals(expected = LoginResult.InvalidPassword, actual = result)
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
    val body = """
      {
        "status": "error",
        "reason": "Some error"
      }
    """.trimIndent()
    mockEngine += { respondJson(body, HttpStatusCode.InternalServerError) }
    val result = loginRequester.logIn(EXAMPLE_PASSWORD)

    // Then a HTTP result
    assertIs<LoginResult.HttpFailure>(result, result.toString())
  }

  private companion object {
    val EXAMPLE_PASSWORD = Password(value = "P@ssw0rd")
    val EXAMPLE_TOKEN = LoginToken(value = "abc123")
    val EXAMPLE_URL = ServerUrl(Protocol.Https, "website.com")
  }
}
