package actual.login.vm

import actual.api.client.AccountApi
import actual.api.client.ActualApis
import actual.api.client.ActualApisStateHolder
import actual.core.connection.ConnectionMonitor
import actual.core.coroutines.TestCoroutineContexts
import actual.core.model.LoginToken
import actual.core.model.Password
import actual.login.prefs.LoginPreferences
import actual.serverurl.prefs.ServerUrlPreferences
import actual.test.MockWebServerRule
import actual.test.buildPreferences
import alakazam.test.core.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import net.lachlanmckee.timberjunit.TimberTestRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertIs

@RunWith(RobolectricTestRunner::class)
internal class LoginRequesterTest {
  @get:Rule
  val mainDispatcherRule = MainDispatcherRule()

  @get:Rule
  val webServerRule = MockWebServerRule()

  @get:Rule
  val timberRule = TimberTestRule.logAllWhenTestFails()!!

  private lateinit var loginRequester: LoginRequester
  private lateinit var apisStateHolder: ActualApisStateHolder
  private lateinit var loginPreferences: LoginPreferences
  private lateinit var serverUrlPreferences: ServerUrlPreferences
  private lateinit var connectionMonitor: ConnectionMonitor

  private fun TestScope.before() {
    val flowPrefs = buildPreferences(mainDispatcherRule.dispatcher)
    serverUrlPreferences = ServerUrlPreferences(flowPrefs)
    loginPreferences = LoginPreferences(flowPrefs)
    apisStateHolder = ActualApisStateHolder()

    connectionMonitor = ConnectionMonitor(
      scope = this,
      apiStateHolder = apisStateHolder,
      serverUrlPreferences = serverUrlPreferences,
    )

    loginRequester = LoginRequester(
      contexts = TestCoroutineContexts(mainDispatcherRule),
      apisStateHolder = apisStateHolder,
      loginPreferences = loginPreferences,
    )
  }

  @Test
  fun `Successful call`() = runTest {
    before()

    // Given a URL is set
    serverUrlPreferences.url.set(webServerRule.serverUrl())
    connectionMonitor.start()
    advanceUntilIdle()

    // When we log in with a successful response
    val body = """{ "status": "ok", "data": { "token": "$EXAMPLE_TOKEN" } } """
    webServerRule.enqueue(body)
    val result = loginRequester.logIn(EXAMPLE_PASSWORD)
    advanceUntilIdle()

    // Then a success response was parsed, and the token was stored in prefs
    assertEquals(expected = LoginResult.Success, actual = result)
    assertEquals(expected = EXAMPLE_TOKEN, actual = loginPreferences.token.get())
  }

  @Test
  fun `Network error`() = runTest {
    before()

    // Given a mock API is provided, which throws a network error when called
    val errorMessage = "something broke"
    val accountApi = mockk<AccountApi>()
    coEvery { accountApi.login(any()) } throws IOException(errorMessage)
    val apis = mockk<ActualApis>()
    every { apis.account } returns accountApi
    apisStateHolder.update { apis }

    // When we log in
    val result = loginRequester.logIn(EXAMPLE_PASSWORD)
    advanceUntilIdle()

    // Then we get a failure result
    assertEquals(expected = LoginResult.NetworkFailure(errorMessage), actual = result)
  }

  @Test
  fun `Invalid password`() = runTest {
    before()

    // Given a URL is set
    serverUrlPreferences.url.set(webServerRule.serverUrl())
    connectionMonitor.start()
    advanceUntilIdle()

    // When we log in with a successful response, but a null token
    val body = """{ "status": "ok", "data": { "token": null } } """
    webServerRule.enqueue(body)
    val result = loginRequester.logIn(EXAMPLE_PASSWORD)
    advanceUntilIdle()

    // Then a success result was parsed, and the token was stored in prefs
    assertEquals(expected = LoginResult.InvalidPassword, actual = result)
  }

  @Test
  fun `HTTP failure`() = runTest {
    before()

    // Given a URL is set
    serverUrlPreferences.url.set(webServerRule.serverUrl())
    connectionMonitor.start()
    advanceUntilIdle()

    // When we log in with a failed response
    val httpMsg = "Internal error"
    webServerRule.enqueue(code = 500, body = httpMsg)
    val result = loginRequester.logIn(EXAMPLE_PASSWORD)
    advanceUntilIdle()

    // Then a HTTP result
    assertIs<LoginResult.HttpFailure>(result)
  }

  private companion object {
    val EXAMPLE_PASSWORD = Password(value = "P@ssw0rd")
    val EXAMPLE_TOKEN = LoginToken(value = "abc123")
  }
}
