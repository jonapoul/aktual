package aktual.account.vm

import aktual.api.client.AccountApi
import aktual.api.client.AccountApiImpl
import aktual.api.model.account.LoginRequest
import aktual.core.model.AvailableLoginMethod
import aktual.core.model.LoginMethod
import aktual.core.model.Password
import aktual.core.model.ServerUrl
import aktual.core.model.Token
import aktual.core.utils.OpenIdRedirects
import aktual.prefs.AppPreferences
import aktual.prefs.AppPreferencesImpl
import aktual.test.assertThatNextEmission
import aktual.test.buildPreferences
import aktual.test.emptyMockEngine
import aktual.test.respondJson
import aktual.test.testHttpClient
import alakazam.test.TestCoroutineContexts
import alakazam.test.unconfinedDispatcher
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNull
import assertk.assertions.prop
import io.ktor.client.engine.mock.MockEngine
import io.ktor.http.content.TextContent
import io.mockk.coEvery
import io.mockk.mockk
import java.io.IOException
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import okio.FileSystem

internal class LoginRequesterTest {
  private lateinit var loginRequester: LoginRequester
  private lateinit var preferences: AppPreferences
  private lateinit var mockEngine: MockEngine.Queue
  private lateinit var fileSystem: FileSystem
  private lateinit var accountApi: AccountApi

  @BeforeTest
  fun before() {
    mockEngine = emptyMockEngine()
    fileSystem = SYSTEM
    accountApi = AccountApiImpl(client = testHttpClient(mockEngine), serverUrl = EXAMPLE_URL)
  }

  @AfterTest
  fun after() {
    mockEngine.close()
  }

  private fun runRequesterTest(
    accountApi: AccountApi = this@LoginRequesterTest.accountApi,
    action: suspend TestScope.() -> Unit,
  ) = runTest {
    val dispatcher = unconfinedDispatcher
    val dataStore = buildPreferences(dispatcher)
    preferences = AppPreferencesImpl(dataStore)

    loginRequester =
      LoginRequester(
        accountApi = accountApi,
        contexts = TestCoroutineContexts(dispatcher),
        preferences = preferences,
        runLevelController = mockk(relaxed = true),
        openIdRedirects = FakeOpenIdRedirects,
      )
    action()
  }

  @Test
  fun `Successful call`() = runRequesterTest {

    // Given a URL is set
    preferences.serverUrl.set(EXAMPLE_URL)

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
  fun `Network error`() =
    runRequesterTest(
      // Given a mock API throws a network error when called
      accountApi =
        mockk { coEvery { login(any<LoginRequest.Password>()) } throws IOException(ERROR_MESSAGE) },
    ) {
      // When we log in
      val result = loginRequester.logIn(EXAMPLE_PASSWORD)

      // Then we get a failure result
      assertThat(result).isEqualTo(LoginResult.NetworkFailure(ERROR_MESSAGE))
    }

  @Test
  fun `Invalid password`() = runRequesterTest {

    // Given a URL is set
    preferences.serverUrl.set(EXAMPLE_URL)

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
    assertThat(result).isEqualTo(InvalidPassword)
  }

  @Test
  fun `HTTP failure with token expired`() = runRequesterTest {

    // Given a URL is set
    preferences.serverUrl.set(EXAMPLE_URL)

    // When we log in with a token-expired error response
    val body =
      """
      {
        "status": "error",
        "reason": "token-expired"
      }
      """
        .trimIndent()
    mockEngine += { respondJson(body, Unauthorized) }
    val result = loginRequester.logIn(EXAMPLE_PASSWORD)

    // Then we get a token expired result
    assertThat(result).isEqualTo(TokenExpired)
  }

  @Test
  fun `HTTP failure with other reason`() = runRequesterTest {

    // Given a URL is set
    preferences.serverUrl.set(EXAMPLE_URL)

    // When we log in with an error response
    val body =
      """
      {
        "status": "error",
        "reason": "some-error"
      }
      """
        .trimIndent()
    mockEngine += { respondJson(body, InternalServerError) }
    val result = loginRequester.logIn(EXAMPLE_PASSWORD)

    // Then we get an other failure result with the reason
    assertThat(result).isEqualTo(LoginResult.OtherFailure("some-error"))
  }

  @Test
  fun `Valid API key`() = runRequesterTest {
    preferences.serverUrl.set(EXAMPLE_URL)

    preferences.token.asFlow().test {
      assertThatNextEmission().isNull()

      // When the server validates the key
      val body = """{ "status": "ok", "data": { "validated": true, "loginMethod": "api-key" } }"""
      mockEngine += { request ->
        assertThat(request.method).isEqualTo(Get)
        assertThat(request.headers["X-ACTUAL-TOKEN"]).isEqualTo(EXAMPLE_API_KEY.value)
        respondJson(body)
      }
      val result = loginRequester.logIn(EXAMPLE_API_KEY)

      // Then the key is stored as the token
      assertThat(result).isEqualTo(LoginResult.Success(EXAMPLE_API_KEY))
      assertThatNextEmission().isEqualTo(EXAMPLE_API_KEY)
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Invalid API key`() = runRequesterTest {
    preferences.serverUrl.set(EXAMPLE_URL)

    // When the server rejects the key
    val body = """{ "status": "error", "reason": "unauthorized" }"""
    mockEngine += { respondJson(body, Unauthorized) }
    val result = loginRequester.logIn(EXAMPLE_API_KEY)

    // Then we get an invalid key result, and nothing is stored
    assertThat(result).isEqualTo(InvalidApiKey)
    assertThat(preferences.token.get()).isNull()
  }

  @Test
  fun `OpenID login sends the app's return URL`() = runRequesterTest {
    preferences.serverUrl.set(EXAMPLE_URL)

    val body = """{ "status": "ok", "data": { "returnUrl": "https://sso.example.com/auth" } }"""
    mockEngine += { request ->
      assertThat(request.body)
        .isInstanceOf<TextContent>()
        .prop(TextContent::text)
        .contains("aktual.test://localhost")
      respondJson(body)
    }
    val result = loginRequester.logIn(EXAMPLE_PASSWORD, loginMethod = OpenId)

    assertThat(result).isEqualTo(LoginResult.Redirect("https://sso.example.com/auth"))
  }

  @Test
  fun `Rejected OpenID token has expired`() = runRequesterTest {
    preferences.serverUrl.set(EXAMPLE_URL)

    val body = """{ "status": "error", "reason": "unauthorized" }"""
    mockEngine += { respondJson(body, Unauthorized) }
    val result = loginRequester.logIn(EXAMPLE_TOKEN)

    assertThat(result).isEqualTo(TokenExpired)
    assertThat(preferences.token.get()).isNull()
  }

  @Test
  fun `First OpenID login asks for the server password`() = runRequesterTest {
    mockEngine += { request ->
      assertThat(request.url.encodedPath).isEqualTo("/admin/owner-created/")
      respondJson("false")
    }

    assertThat(loginRequester.openIdFirstLogin(listOf(PASSWORD_METHOD, OPENID_METHOD)))
      .isEqualTo(FirstWithPassword)
  }

  @Test
  fun `First OpenID login without password login`() = runRequesterTest {
    mockEngine += { respondJson("false") }

    assertThat(loginRequester.openIdFirstLogin(listOf(OPENID_METHOD))).isEqualTo(First)
  }

  @Test
  fun `OpenID login once there's an owner`() = runRequesterTest {
    mockEngine += { respondJson("true") }

    assertThat(loginRequester.openIdFirstLogin(listOf(PASSWORD_METHOD, OPENID_METHOD)))
      .isEqualTo(NotFirst)
  }

  @Test
  fun `Owner check failing assumes there's an owner`() =
    runRequesterTest(
      accountApi = mockk { coEvery { ownerCreated() } throws IOException(ERROR_MESSAGE) },
    ) {
      assertThat(loginRequester.openIdFirstLogin(listOf(PASSWORD_METHOD, OPENID_METHOD)))
        .isEqualTo(NotFirst)
    }

  @Test
  fun `API key network error`() =
    runRequesterTest(
      accountApi = mockk { coEvery { validate(any()) } throws IOException(ERROR_MESSAGE) },
    ) {
      val result = loginRequester.logIn(EXAMPLE_API_KEY)

      assertThat(result).isEqualTo(LoginResult.NetworkFailure(ERROR_MESSAGE))
    }

  private object FakeOpenIdRedirects : OpenIdRedirects {
    override val tokens = emptyFlow<Token>()

    override suspend fun returnUrl() = "aktual.test://localhost"
  }

  private companion object {
    const val ERROR_MESSAGE = "something broke"
    val EXAMPLE_API_KEY = Token(value = "actual_abc123")
    val EXAMPLE_PASSWORD = Password(value = "P@ssw0rd")
    val EXAMPLE_TOKEN = Token(value = "abc123")
    val EXAMPLE_URL = ServerUrl(Https, "website.com")
    val PASSWORD_METHOD = AvailableLoginMethod(LoginMethod.Password, "Password", isActive = true)
    val OPENID_METHOD = AvailableLoginMethod(OpenId, "OpenID", isActive = false)
  }
}
