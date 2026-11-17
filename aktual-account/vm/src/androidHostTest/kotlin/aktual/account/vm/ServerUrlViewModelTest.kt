package aktual.account.vm

import aktual.core.model.ServerUrl
import aktual.core.model.Token
import aktual.core.utils.LocalNetworkPermission
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
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.prop
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.createDynamicGraph
import io.ktor.client.engine.mock.MockEngine
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
  private val localNetworkPermission = FakeLocalNetworkPermission()

  @AfterTest
  fun after() {
    mockEngine.close()
    appGraph.close()
  }

  private fun runServerUrlTest(action: suspend TestScope.() -> Unit) = runTest {
    mockEngine = emptyMockEngine()

    val contexts = TestCoroutineContexts(standardDispatcher)
    appGraph =
      createDynamicGraph<TestAppGraph>(
        TestCoroutineContainer(backgroundScope, contexts),
        TestHttpContainer(mockEngine),
        FakeLocalNetworkContainer(localNetworkPermission),
      )

    appGraph.preferences.serverUrl.set(EXAMPLE_URL)
    appGraph.runLevelController.init(listOf(appGraph))

    advanceUntilIdle()

    viewModel =
      appGraph.metroViewModelFactory.create(ServerUrlViewModel::class, CreationExtras.Empty)
    action()
  }

  @Test
  fun `Nav to login when typing and clicking confirm if already bootstrapped`() = runServerUrlTest {
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
      assertThatNextEmissionIsEqualTo(ToLogin)
      advanceUntilIdle()
      ensureAllEventsConsumed()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Nav to bootstrap when typing and clicking confirm if not bootstrapped`() = runServerUrlTest {
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
      assertThatNextEmissionIsEqualTo(ToBootstrap)
      advanceUntilIdle()
      ensureAllEventsConsumed()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Set initial parameters based on preferences`() = runServerUrlTest {
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
  fun `Show error message if bootstrap request gives failure response`() = runServerUrlTest {
    viewModel.error.test {
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
      mockEngine += { respondJson(body, BadRequest) }

      // When
      viewModel.onSelectProtocol(EXAMPLE_URL.protocol)
      viewModel.onEnterUrl(EXAMPLE_URL.baseUrl)
      viewModel.onClickConfirm()

      // Then
      assertThatNextEmission()
        .isNotNull()
        .isInstanceOf<ServerUrlError.Failed>()
        .prop(ServerUrlError.Failed::message)
        .contains(reason)
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Show error message if bootstrap request fails`() = runServerUrlTest {
    viewModel.error.test {
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
      assertThatNextEmission()
        .isNotNull()
        .isInstanceOf<ServerUrlError.Failed>()
        .prop(ServerUrlError.Failed::message)
        .contains(reason)
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Clear saved token if the confirmed URL is different from previously-saved`() =
    runServerUrlTest {
      appGraph.preferences.token.asFlow().test {
        // Given no token initially saved
        assertThatNextEmission().isNull()

        // when we save a token and a URL
        val initialUrl = ServerUrl(Https, "website.com")
        appGraph.preferences.serverUrl.set(initialUrl)
        val token = Token("abc-123")
        appGraph.preferences.token.set(token)

        // then the token has been saved
        assertThatNextEmissionIsEqualTo(token)

        // when we enter a different url and click confirm
        val secondUrl = ServerUrl(Http, "some.other.website.com")
        assertThat(initialUrl).isNotEqualTo(secondUrl)
        viewModel.onSelectProtocol(secondUrl.protocol)
        viewModel.onEnterUrl(secondUrl.baseUrl)
        viewModel.onClickConfirm()

        // then the saved token is cleared
        assertThatNextEmission().isNull()
        cancelAndIgnoreRemainingEvents()
      }
    }

  @Test
  fun `Request local network permission before connecting`() = runServerUrlTest {
    viewModel.localNetworkPermissionRequests.receiveAsFlow().test {
      // Given the permission is needed for this server
      advanceUntilIdle()
      localNetworkPermission.isMissing = true
      setBootstrapResponse(bootstrapped = true)

      // When
      viewModel.onSelectProtocol(EXAMPLE_URL.protocol)
      viewModel.onEnterUrl(EXAMPLE_URL.baseUrl)
      viewModel.onClickConfirm()

      // Then the permission is requested without connecting yet
      awaitItem()
      advanceUntilIdle()
      assertThat(mockEngine.requestHistory).isEmpty()
      cancelAndIgnoreRemainingEvents()
    }

    viewModel.navDestination.receiveAsFlow().test {
      // When it's given
      localNetworkPermission.isMissing = false
      viewModel.onLocalNetworkPermissionResult(isGranted = true)

      // Then we connect
      assertThatNextEmissionIsEqualTo(ToLogin)
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Ignore permission result without a pending request`() = runServerUrlTest {
    // Given a new VM, e.g. after process death while the permission dialog was showing
    advanceUntilIdle()
    setBootstrapResponse(bootstrapped = true)

    // When the result arrives
    viewModel.onLocalNetworkPermissionResult(isGranted = true)
    advanceUntilIdle()

    // Then nothing connects
    assertThat(mockEngine.requestHistory).isEmpty()
    viewModel.isLoading.test { assertThatNextEmission().isFalse() }
  }

  @Test
  fun `Clearing state drops an unanswered permission request`() = runServerUrlTest {
    // Given a permission request the UI hasn't received
    advanceUntilIdle()
    localNetworkPermission.isMissing = true
    setBootstrapResponse(bootstrapped = true)
    viewModel.onSelectProtocol(EXAMPLE_URL.protocol)
    viewModel.onEnterUrl(EXAMPLE_URL.baseUrl)
    viewModel.onClickConfirm()
    advanceUntilIdle()

    // When the screen leaves
    viewModel.clearState()

    // Then nothing is left to request, and a late result doesn't connect
    assertThat(viewModel.localNetworkPermissionRequests.tryReceive().isSuccess).isFalse()
    viewModel.onLocalNetworkPermissionResult(isGranted = true)
    advanceUntilIdle()
    assertThat(mockEngine.requestHistory).isEmpty()
  }

  @Test
  fun `Show local network error if connecting fails without the permission`() = runServerUrlTest {
    viewModel.error.test {
      // Given the permission is needed but denied, and the connection fails
      assertThatNextEmission().isNull()
      localNetworkPermission.isMissing = true
      mockEngine.clear()
      mockEngine += { throw IOException("Timed out") }

      // When
      viewModel.onSelectProtocol(EXAMPLE_URL.protocol)
      viewModel.onEnterUrl(EXAMPLE_URL.baseUrl)
      viewModel.onClickConfirm()
      advanceUntilIdle()
      viewModel.onLocalNetworkPermissionResult(isGranted = false)

      // Then
      assertThatNextEmission().isEqualTo(LocalNetworkBlocked)
      assertThat(localNetworkPermission.requiredChecks).isEqualTo(1)
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

  private class FakeLocalNetworkPermission : LocalNetworkPermission {
    var isMissing = false

    var requiredChecks = 0

    override fun isGranted(): Boolean = !isMissing

    override suspend fun isRequiredFor(url: ServerUrl): Boolean {
      requiredChecks++
      return true
    }

    override suspend fun isRequiredForHost(host: String): Boolean = true
  }

  @BindingContainer
  private class FakeLocalNetworkContainer(private val permission: LocalNetworkPermission) {
    @Provides fun permission(): LocalNetworkPermission = permission
  }

  private companion object {
    val EXAMPLE_URL = ServerUrl(Http, "website.com")
  }
}
