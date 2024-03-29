package dev.jonpoulton.actual.login.vm

import alakazam.kotlin.core.IODispatcher
import alakazam.test.core.CoroutineRule
import dev.jonpoulton.actual.api.client.AccountApi
import dev.jonpoulton.actual.api.client.ActualApis
import dev.jonpoulton.actual.api.client.ActualApisStateHolder
import dev.jonpoulton.actual.core.connection.ConnectionMonitor
import dev.jonpoulton.actual.core.model.LoginToken
import dev.jonpoulton.actual.core.model.Password
import dev.jonpoulton.actual.login.prefs.LoginPreferences
import dev.jonpoulton.actual.serverurl.prefs.ServerUrlPreferences
import dev.jonpoulton.actual.test.buildFlowPreferences
import dev.jonpoulton.actual.test.http.MockWebServerRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import net.lachlanmckee.timberjunit.TimberTestRule
import org.junit.Before
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
  val coroutineRule = CoroutineRule()

  @get:Rule
  val webServerRule = MockWebServerRule()

  @get:Rule
  val timberRule = TimberTestRule.logAllWhenTestFails()!!

  private lateinit var loginRequester: LoginRequester
  private lateinit var apisStateHolder: ActualApisStateHolder
  private lateinit var loginPreferences: LoginPreferences
  private lateinit var serverUrlPreferences: ServerUrlPreferences
  private lateinit var connectionMonitor: ConnectionMonitor

  @Before
  fun before() {
    val flowPrefs = buildFlowPreferences(coroutineRule.dispatcher)
    serverUrlPreferences = ServerUrlPreferences(flowPrefs)
    loginPreferences = LoginPreferences(flowPrefs)
    apisStateHolder = ActualApisStateHolder()

    connectionMonitor = ConnectionMonitor(
      scope = coroutineRule.scope,
      apiStateHolder = apisStateHolder,
      serverUrlPreferences = serverUrlPreferences,
    )

    loginRequester = LoginRequester(
      io = IODispatcher(coroutineRule.dispatcher),
      apisStateHolder = apisStateHolder,
      loginPreferences = loginPreferences,
    )
  }

  @Test
  fun `Successful call`() = runTest {
    // Given a URL is set
    serverUrlPreferences.url.set(webServerRule.serverUrl())
    connectionMonitor.start()
    coroutineRule.advanceUntilIdle()

    // When we log in with a successful response
    val body = """{ "status": "ok", "data": { "token": "$EXAMPLE_TOKEN" } } """
    webServerRule.enqueue(body)
    val result = loginRequester.logIn(EXAMPLE_PASSWORD)
    coroutineRule.advanceUntilIdle()

    // Then a success response was parsed, and the token was stored in prefs
    assertEquals(expected = LoginResult.Success, actual = result)
    assertEquals(expected = EXAMPLE_TOKEN, actual = loginPreferences.token.get())
  }

  @Test
  fun `Network error`() = runTest {
    // Given a mock API is provided, which throws a network error when called
    val errorMessage = "something broke"
    val accountApi = mockk<AccountApi>()
    coEvery { accountApi.login(any()) } throws IOException(errorMessage)
    val apis = mockk<ActualApis>()
    every { apis.account } returns accountApi
    apisStateHolder.set(apis)

    // When we log in
    val result = loginRequester.logIn(EXAMPLE_PASSWORD)
    coroutineRule.advanceUntilIdle()

    // Then we get a failure result
    assertEquals(expected = LoginResult.NetworkFailure(errorMessage), actual = result)
  }

  @Test
  fun `Invalid password`() = runTest {
    // Given a URL is set
    serverUrlPreferences.url.set(webServerRule.serverUrl())
    connectionMonitor.start()
    coroutineRule.advanceUntilIdle()

    // When we log in with a successful response, but a null token
    val body = """{ "status": "ok", "data": { "token": null } } """
    webServerRule.enqueue(body)
    val result = loginRequester.logIn(EXAMPLE_PASSWORD)
    coroutineRule.advanceUntilIdle()

    // Then a success result was parsed, and the token was stored in prefs
    assertEquals(expected = LoginResult.InvalidPassword, actual = result)
  }

  @Test
  fun `HTTP failure`() = runTest {
    // Given a URL is set
    serverUrlPreferences.url.set(webServerRule.serverUrl())
    connectionMonitor.start()
    coroutineRule.advanceUntilIdle()

    // When we log in with a failed response
    val httpMsg = "Internal error"
    webServerRule.enqueue(code = 500, body = httpMsg)
    val result = loginRequester.logIn(EXAMPLE_PASSWORD)
    coroutineRule.advanceUntilIdle()

    // Then a HTTP result
    assertIs<LoginResult.HttpFailure>(result)
  }

  private companion object {
    val EXAMPLE_PASSWORD = Password(value = "P@ssw0rd")
    val EXAMPLE_TOKEN = LoginToken(value = "abc123")
  }
}
