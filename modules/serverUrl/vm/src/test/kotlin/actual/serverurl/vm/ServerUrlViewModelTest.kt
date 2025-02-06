package actual.serverurl.vm

import actual.api.client.AccountApi
import actual.api.client.ActualApis
import actual.api.client.ActualApisStateHolder
import actual.api.model.account.NeedsBootstrapResponse
import actual.core.coroutines.TestCoroutineContexts
import actual.core.model.Protocol
import actual.core.model.ServerUrl
import actual.core.state.ActualVersionsStateHolder
import actual.serverurl.prefs.ServerUrlPreferences
import actual.test.TestBuildConfig
import actual.test.buildPreferences
import alakazam.test.core.MainDispatcherRule
import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import net.lachlanmckee.timberjunit.TimberTestRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertNull

@RunWith(RobolectricTestRunner::class)
class ServerUrlViewModelTest {
  @get:Rule
  val mainDispatcherRule = MainDispatcherRule()

  @get:Rule
  val timberRule = TimberTestRule.logAllWhenTestFails()!!

  // Real
  private lateinit var preferences: ServerUrlPreferences
  private lateinit var viewModel: ServerUrlViewModel
  private lateinit var apisStateHolder: ActualApisStateHolder
  private lateinit var versionsStateHolder: ActualVersionsStateHolder

  // Mock
  private lateinit var apis: ActualApis
  private lateinit var accountApi: AccountApi

  @Before
  fun before() {
    val prefs = buildPreferences(mainDispatcherRule.dispatcher)
    preferences = ServerUrlPreferences(prefs)
    versionsStateHolder = ActualVersionsStateHolder(TestBuildConfig)

    accountApi = mockk()
    apis = mockk {
      every { serverUrl } returns EXAMPLE_URL
      every { account } returns accountApi
    }
    apisStateHolder = ActualApisStateHolder()
    apisStateHolder.update { apis }
  }

  private fun buildViewModel() {
    viewModel = ServerUrlViewModel(
      contexts = TestCoroutineContexts(mainDispatcherRule),
      apiStateHolder = apisStateHolder,
      prefs = preferences,
      versionsStateHolder = versionsStateHolder,
    )
  }

  @Test
  fun `Nav to login when typing and clicking confirm if already bootstrapped`() = runTest {
    buildViewModel()
    viewModel.shouldNavigate.test {
      // Given we're currently not navigating, and the API returns that we're bootstrapped
      assertNull(awaitItem())
      setBootstrapResponse(bootstrapped = true)

      // When
      viewModel.onSelectProtocol(EXAMPLE_URL.protocol)
      viewModel.onEnterUrl(EXAMPLE_URL.baseUrl)
      viewModel.onClickConfirm()

      // Then
      assertEquals(expected = ShouldNavigate.ToLogin, actual = awaitItem())
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Nav to bootstrap when typing and clicking confirm if not bootstrapped`() = runTest {
    buildViewModel()
    viewModel.shouldNavigate.test {
      // Given we're currently not navigating, and the API returns that we're not bootstrapped
      assertNull(awaitItem())
      setBootstrapResponse(bootstrapped = false)

      // When
      viewModel.onSelectProtocol(EXAMPLE_URL.protocol)
      viewModel.onEnterUrl(EXAMPLE_URL.baseUrl)
      viewModel.onClickConfirm()

      // Then
      assertEquals(expected = ShouldNavigate.ToBootstrap, actual = awaitItem())
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Set initial parameters based on preferences`() = runTest {
    // Given
    preferences.url.set(EXAMPLE_URL)

    // When
    buildViewModel()
    advanceUntilIdle()

    // Then
    val protocol = viewModel.protocol.value
    val baseUrl = viewModel.baseUrl.value
    assertEquals(expected = EXAMPLE_URL.protocol, actual = protocol)
    assertEquals(expected = EXAMPLE_URL.baseUrl, actual = baseUrl)
  }

  @Test
  fun `Show error message if bootstrap request gives failure response`() = runTest {
    buildViewModel()
    viewModel.errorMessage.test {
      // Given we're currently not navigating, and the API returns that we're not bootstrapped
      assertNull(awaitItem())
      val reason = "SOMETHING BROKE"
      coEvery { accountApi.needsBootstrap() } returns NeedsBootstrapResponse.Error(reason = reason)

      // When
      viewModel.onSelectProtocol(EXAMPLE_URL.protocol)
      viewModel.onEnterUrl(EXAMPLE_URL.baseUrl)
      viewModel.onClickConfirm()

      // Then
      val emitted = awaitItem()
      assertEquals(
        expected = true,
        actual = emitted?.contains(reason),
        message = "Received $emitted",
      )
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Show error message if bootstrap request fails`() = runTest {
    buildViewModel()
    viewModel.errorMessage.test {
      // Given we're currently not navigating, and the API returns that we're not bootstrapped
      assertNull(awaitItem())
      val reason = "SOMETHING BROKE"
      coEvery { accountApi.needsBootstrap() } throws IOException(reason)

      // When
      viewModel.onSelectProtocol(EXAMPLE_URL.protocol)
      viewModel.onEnterUrl(EXAMPLE_URL.baseUrl)
      viewModel.onClickConfirm()

      // Then
      val emitted = awaitItem()
      assertEquals(
        expected = true,
        actual = emitted?.contains(reason),
        message = "Received $emitted",
      )
      cancelAndIgnoreRemainingEvents()
    }
  }

  private fun setBootstrapResponse(bootstrapped: Boolean) {
    coEvery { accountApi.needsBootstrap() } returns NeedsBootstrapResponse.Ok(
      data = NeedsBootstrapResponse.Data(
        bootstrapped = bootstrapped,
      ),
    )
  }

  private companion object {
    val EXAMPLE_URL = ServerUrl(Protocol.Http, "website.com")
  }
}
