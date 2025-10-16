@file:OptIn(ExperimentalCoroutinesApi::class)

package actual.account.vm

import actual.api.client.AccountApi
import actual.api.client.ActualApis
import actual.api.client.ActualApisStateHolder
import actual.api.client.ActualJson
import actual.core.model.ActualVersionsStateHolder
import actual.core.model.LoginToken
import actual.core.model.Protocol
import actual.core.model.ServerUrl
import actual.prefs.AppGlobalPreferences
import actual.test.TestBuildConfig
import actual.test.assertThatNextEmission
import actual.test.assertThatNextEmissionIsEqualTo
import actual.test.buildPreferences
import actual.test.clear
import actual.test.emptyMockEngine
import actual.test.respondJson
import actual.test.testHttpClient
import alakazam.test.core.TestCoroutineContexts
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import io.ktor.client.engine.mock.MockEngine
import io.ktor.http.HttpStatusCode
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.IOException
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@RunWith(RobolectricTestRunner::class)
class ServerUrlViewModelTest {
  // Real
  private lateinit var preferences: AppGlobalPreferences
  private lateinit var viewModel: ServerUrlViewModel
  private lateinit var apisStateHolder: ActualApisStateHolder
  private lateinit var versionsStateHolder: ActualVersionsStateHolder

  // Mock
  private lateinit var apis: ActualApis
  private lateinit var accountApi: AccountApi
  private lateinit var mockEngine: MockEngine.Queue

  @BeforeTest
  fun before() {
    versionsStateHolder = ActualVersionsStateHolder(TestBuildConfig)
    mockEngine = emptyMockEngine()
    accountApi = AccountApi(EXAMPLE_URL, testHttpClient(mockEngine, ActualJson))
    apis = mockk {
      every { close() } just runs
      every { serverUrl } returns EXAMPLE_URL
      every { account } returns accountApi
    }
    apisStateHolder = ActualApisStateHolder()
    apisStateHolder.update { apis }
  }

  @AfterTest
  fun after() {
    mockEngine.close()
  }

  private fun TestScope.buildPreferences() {
    val prefs = buildPreferences(UnconfinedTestDispatcher(testScheduler))
    preferences = AppGlobalPreferences(prefs)
    preferences.serverUrl.set(EXAMPLE_URL)
  }

  private fun TestScope.buildViewModel() {
    viewModel = ServerUrlViewModel(
      contexts = TestCoroutineContexts(StandardTestDispatcher(testScheduler)),
      apiStateHolder = apisStateHolder,
      preferences = preferences,
      versionsStateHolder = versionsStateHolder,
      buildConfig = TestBuildConfig.copy(defaultServerUrl = null),
    )
  }

  @Test
  fun `Nav to login when typing and clicking confirm if already bootstrapped`() = runTest {
    buildPreferences()
    buildViewModel()
    viewModel.navDestination.receiveAsFlow().test {
      // Given we're currently not navigating, and the API returns that we're bootstrapped
      advanceUntilIdle()
      ensureAllEventsConsumed()
      setBootstrapResponse(bootstrapped = true)

      // When
      viewModel.onSelectProtocol(EXAMPLE_URL.protocol)
      viewModel.onEnterUrl(EXAMPLE_URL.baseUrl)
      viewModel.onClickConfirm()

      // Then
      assertThatNextEmissionIsEqualTo(NavDestination.ToLogin)
      advanceUntilIdle()
      ensureAllEventsConsumed()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Nav to bootstrap when typing and clicking confirm if not bootstrapped`() = runTest {
    buildPreferences()
    buildViewModel()
    viewModel.navDestination.receiveAsFlow().test {
      // Given we're currently not navigating, and the API returns that we're not bootstrapped
      advanceUntilIdle()
      ensureAllEventsConsumed()
      setBootstrapResponse(bootstrapped = false)

      // When
      viewModel.onSelectProtocol(EXAMPLE_URL.protocol)
      viewModel.onEnterUrl(EXAMPLE_URL.baseUrl)
      viewModel.onClickConfirm()

      // Then
      assertThatNextEmissionIsEqualTo(NavDestination.ToBootstrap)
      advanceUntilIdle()
      ensureAllEventsConsumed()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Set initial parameters based on preferences`() = runTest {
    // Given
    buildPreferences()
    preferences.serverUrl.set(EXAMPLE_URL)

    // When
    buildViewModel()
    advanceUntilIdle()

    // Then
    val protocol = viewModel.protocol.value
    val baseUrl = viewModel.baseUrl.value
    assertThat(protocol).isEqualTo(EXAMPLE_URL.protocol)
    assertThat(baseUrl).isEqualTo(EXAMPLE_URL.baseUrl)
  }

  @Test
  fun `Show error message if bootstrap request gives failure response`() = runTest {
    buildPreferences()
    buildViewModel()

    viewModel.errorMessage.test {
      // Given we're currently not navigating
      assertThatNextEmission().isNull()

      // and the API returns failure
      val reason = "SOMETHING BROKE"
      val body = """
        {
          "status": "error",
          "reason": "$reason"
        }
      """.trimIndent()
      mockEngine.clear()
      mockEngine += { respondJson(body, HttpStatusCode.BadRequest) }

      // When
      viewModel.onSelectProtocol(EXAMPLE_URL.protocol)
      viewModel.onEnterUrl(EXAMPLE_URL.baseUrl)
      viewModel.onClickConfirm()

      // Then
      assertThat(awaitItem())
        .isNotNull()
        .contains(reason)
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Show error message if bootstrap request fails`() = runTest {
    buildPreferences()
    buildViewModel()
    viewModel.errorMessage.test {
      // Given we're currently not navigating, and the API returns that we're not bootstrapped
      assertThatNextEmission().isNull()

      // and the API fails
      val reason = "SOMETHING BROKE"
      mockEngine.clear()
      mockEngine += { throw IOException(reason) }

      // When
      viewModel.onSelectProtocol(EXAMPLE_URL.protocol)
      viewModel.onEnterUrl(EXAMPLE_URL.baseUrl)
      viewModel.onClickConfirm()

      // Then
      assertThat(awaitItem())
        .isNotNull()
        .contains(reason)
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Clear saved token if the confirmed URL is different from previously-saved`() = runTest {
    buildPreferences()
    buildViewModel()
    preferences.loginToken.asFlow().test {
      // Given no token initially saved
      assertThatNextEmission().isNull()

      // when we save a token and a URL
      val initialUrl = ServerUrl(Protocol.Https, "website.com")
      preferences.serverUrl.setAndCommit(initialUrl)
      val token = LoginToken("abc-123")
      preferences.loginToken.setAndCommit(token)

      // then the token has been saved
      assertThatNextEmissionIsEqualTo(token)

      // when we enter a different url and click confirm
      val secondUrl = ServerUrl(Protocol.Http, "some.other.website.com")
      assertThat(initialUrl).isNotEqualTo(secondUrl)
      viewModel.onSelectProtocol(secondUrl.protocol)
      viewModel.onEnterUrl(secondUrl.baseUrl)
      viewModel.onClickConfirm()

      // then the saved token is cleared
      assertThatNextEmission().isNull()
      cancelAndIgnoreRemainingEvents()
    }
  }

  private fun setBootstrapResponse(bootstrapped: Boolean) {
    val json = """
      {
          "status": "ok",
          "data": {
              "bootstrapped": $bootstrapped,
              "loginMethod": "password",
              "availableLoginMethods": [
                  {
                      "method": "password",
                      "active": 1,
                      "displayName": "Password"
                  }
              ],
              "multiuser": false
          }
      }
    """.trimIndent()
    mockEngine.clear()
    mockEngine += { respondJson(json) }
  }

  private companion object {
    val EXAMPLE_URL = ServerUrl(Protocol.Http, "website.com")
  }
}
