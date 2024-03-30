package dev.jonpoulton.actual.serverurl.vm

import alakazam.kotlin.core.IODispatcher
import alakazam.test.core.CoroutineRule
import app.cash.turbine.test
import dev.jonpoulton.actual.api.client.AccountApi
import dev.jonpoulton.actual.api.client.ActualApis
import dev.jonpoulton.actual.api.client.ActualApisStateHolder
import dev.jonpoulton.actual.api.model.account.NeedsBootstrapResponse
import dev.jonpoulton.actual.core.model.Protocol
import dev.jonpoulton.actual.core.model.ServerUrl
import dev.jonpoulton.actual.core.state.ActualVersionsStateHolder
import dev.jonpoulton.actual.serverurl.prefs.ServerUrlPreferences
import dev.jonpoulton.actual.test.TestBuildConfig
import dev.jonpoulton.actual.test.buildFlowPreferences
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
import kotlin.test.assertNull

@RunWith(RobolectricTestRunner::class)
class ServerUrlViewModelTest {
  @get:Rule
  val coroutineRule = CoroutineRule()

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
    val flowPrefs = buildFlowPreferences(coroutineRule.dispatcher)
    preferences = ServerUrlPreferences(flowPrefs)
    versionsStateHolder = ActualVersionsStateHolder(TestBuildConfig)

    accountApi = mockk()
    apis = mockk {
      every { serverUrl } returns EXAMPLE_URL
      every { account } returns accountApi
    }
    apisStateHolder = ActualApisStateHolder()
    apisStateHolder.set(apis)
  }

  private fun buildViewModel() {
    viewModel = ServerUrlViewModel(
      io = IODispatcher(coroutineRule.dispatcher),
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
      viewModel.onProtocolSelected(EXAMPLE_URL.protocol)
      viewModel.onUrlEntered(EXAMPLE_URL.baseUrl)
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
      viewModel.onProtocolSelected(EXAMPLE_URL.protocol)
      viewModel.onUrlEntered(EXAMPLE_URL.baseUrl)
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
    coroutineRule.advanceUntilIdle()

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
      viewModel.onProtocolSelected(EXAMPLE_URL.protocol)
      viewModel.onUrlEntered(EXAMPLE_URL.baseUrl)
      viewModel.onClickConfirm()

      // Then
      val emitted = awaitItem()
      assertEquals(expected = true, actual = emitted?.contains(reason), message = "Received $emitted")
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
      viewModel.onProtocolSelected(EXAMPLE_URL.protocol)
      viewModel.onUrlEntered(EXAMPLE_URL.baseUrl)
      viewModel.onClickConfirm()

      // Then
      val emitted = awaitItem()
      assertEquals(expected = true, actual = emitted?.contains(reason), message = "Received $emitted")
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
