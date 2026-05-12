package aktual.account.vm

import aktual.core.model.Protocol
import aktual.core.model.ServerUrl
import aktual.core.model.Token
import aktual.di.AppGraph
import aktual.di.AppScope
import aktual.di.RunLevelController
import aktual.prefs.AppPreferences
import aktual.test.TestCoroutineContainer
import aktual.test.TestHttpContainer
import aktual.test.assertThatNextEmission
import aktual.test.assertThatNextEmissionIsEqualTo
import aktual.test.clear
import aktual.test.emptyMockEngine
import aktual.test.respondJson
import alakazam.test.TestCoroutineContexts
import alakazam.test.standardDispatcher
import androidx.lifecycle.viewmodel.CreationExtras
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.createDynamicGraph
import io.ktor.client.engine.mock.MockEngine
import io.ktor.http.HttpStatusCode
import java.io.IOException
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ServerUrlViewModelTest {
  // Real
  private lateinit var viewModel: ServerUrlViewModel
  private lateinit var appGraph: TestAppGraph

  // Mock
  private lateinit var mockEngine: MockEngine.Queue

  @AfterTest
  fun after() {
    mockEngine.close()
    appGraph.close()
  }

  private suspend fun TestScope.before() {
    mockEngine = emptyMockEngine()

    val contexts = TestCoroutineContexts(standardDispatcher)
    appGraph =
      createDynamicGraph<TestAppGraph>(
        TestCoroutineContainer(backgroundScope, contexts),
        TestHttpContainer(mockEngine),
      )

    appGraph.preferences.serverUrl.set(EXAMPLE_URL)
    appGraph.runLevelController.init(listOf(appGraph))

    advanceUntilIdle()

    viewModel =
      appGraph.metroViewModelFactory.create(ServerUrlViewModel::class, CreationExtras.Empty)
  }

  @Test
  fun `Nav to login when typing and clicking confirm if already bootstrapped`() = runTest {
    before()
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
    before()
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
    before()

    // When
    advanceUntilIdle()

    // Then
    viewModel.protocol.test {
      var protocol = awaitItem()
      while (protocol != EXAMPLE_URL.protocol) {
        protocol = awaitItem()
      }
    }
    viewModel.baseUrl.test { assertThatNextEmission().isEqualTo(EXAMPLE_URL.baseUrl) }
  }

  @Test
  fun `Show error message if bootstrap request gives failure response`() = runTest {
    before()

    viewModel.errorMessage.test {
      // Given we're currently not navigating
      assertThatNextEmission().isNull()

      // and the API returns failure
      val reason = "SOMETHING BROKE"
      val body =
        """
        {
          "status": "error",
          "reason": "$reason"
        }
      """
          .trimIndent()
      mockEngine.clear()
      mockEngine += { respondJson(body, HttpStatusCode.BadRequest) }

      // When
      viewModel.onSelectProtocol(EXAMPLE_URL.protocol)
      viewModel.onEnterUrl(EXAMPLE_URL.baseUrl)
      viewModel.onClickConfirm()

      // Then
      assertThatNextEmission().isNotNull().contains(reason)
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Show error message if bootstrap request fails`() = runTest {
    before()
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
      assertThatNextEmission().isNotNull().contains(reason)
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Clear saved token if the confirmed URL is different from previously-saved`() = runTest {
    before()
    appGraph.preferences.token.asFlow().test {
      // Given no token initially saved
      assertThatNextEmission().isNull()

      // when we save a token and a URL
      val initialUrl = ServerUrl(Protocol.Https, "website.com")
      appGraph.preferences.serverUrl.set(initialUrl)
      val token = Token("abc-123")
      appGraph.preferences.token.set(token)

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
    val json =
      """
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
    """
        .trimIndent()
    mockEngine.clear()
    mockEngine += { respondJson(json) }
  }

  @DependencyGraph(AppScope::class)
  internal interface TestAppGraph : AppGraph {
    val runLevelController: RunLevelController
    val preferences: AppPreferences
  }

  private companion object {
    val EXAMPLE_URL = ServerUrl(Protocol.Http, "website.com")
  }
}
