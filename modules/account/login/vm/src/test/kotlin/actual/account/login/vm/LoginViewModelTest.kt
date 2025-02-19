package actual.account.login.vm

import actual.account.login.domain.LoginRequester
import actual.account.login.domain.LoginResult
import actual.account.login.prefs.LoginPreferences
import actual.account.model.LoginToken
import actual.core.colorscheme.ColorSchemePreferences
import actual.core.versions.ActualVersionsStateHolder
import actual.log.EmptyLogger
import actual.test.TestBuildConfig
import actual.test.buildPreferences
import actual.url.model.Protocol
import actual.url.model.ServerUrl
import actual.url.prefs.ServerUrlPreferences
import alakazam.test.core.MainDispatcherRule
import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
internal class LoginViewModelTest {
  @get:Rule
  val mainDispatcherRule = MainDispatcherRule()

  // real
  private lateinit var serverUrlPrefs: ServerUrlPreferences
  private lateinit var loginPrefs: LoginPreferences
  private lateinit var colorSchemePreferences: ColorSchemePreferences
  private lateinit var viewModel: LoginViewModel
  private lateinit var versionsStateHolder: ActualVersionsStateHolder

  // mock
  private lateinit var loginRequester: LoginRequester

  @Before
  fun before() {
    val prefs = buildPreferences(mainDispatcherRule.dispatcher)
    serverUrlPrefs = ServerUrlPreferences(prefs)
    loginPrefs = LoginPreferences(prefs)
    colorSchemePreferences = ColorSchemePreferences(prefs)

    versionsStateHolder = ActualVersionsStateHolder(TestBuildConfig)
    loginRequester = mockk(relaxed = true)

    viewModel = LoginViewModel(
      logger = EmptyLogger,
      versionsStateHolder = versionsStateHolder,
      loginRequester = loginRequester,
      serverUrlPrefs = serverUrlPrefs,
      loginPrefs = loginPrefs,
      colorSchemePreferences = colorSchemePreferences,
    )
  }

  @Test
  fun `Enter password`() = runTest {
    viewModel.enteredPassword.test {
      assertEquals(expected = "", actual = awaitItem().toString())

      viewModel.onEnterPassword(password = "hello world")
      assertEquals(expected = "hello world", actual = awaitItem().toString())

      viewModel.clearState()
      assertEquals(expected = "", actual = awaitItem().toString())

      expectNoEvents()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Server URL`() = runTest {
    viewModel.serverUrl.test {
      assertEquals(expected = ServerUrl.Demo, actual = awaitItem())

      val url = ServerUrl(Protocol.Https, baseUrl = "url.for.my.server.com")
      serverUrlPrefs.url.set(url)

      assertEquals(expected = url, actual = awaitItem())
      expectNoEvents()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Signing in with invalid password`() = runTest {
    combine(viewModel.loginFailure, viewModel.isLoading, ::Pair)
      .distinctUntilChanged()
      .test {
        // Initially not loading or failed
        val (failure1, loading1) = awaitItem()
        assertNull(failure1)
        assertFalse(loading1)

        // When we make the failing request
        coEvery { loginRequester.logIn(any()) } returns LoginResult.InvalidPassword
        viewModel.onClickSignIn()

        // Then we're loading but not failed
        val (failure2, loading2) = awaitItem()
        assertNull(failure2)
        assertTrue(loading2)

        // Then not loading
        val (failure3, loading3) = awaitItem()
        assertNull(failure3)
        assertFalse(loading3)

        // Then failed
        val (failure4, loading4) = awaitItem()
        assertEquals(expected = LoginResult.InvalidPassword, actual = failure4)
        assertFalse(loading4)

        expectNoEvents()
        cancelAndIgnoreRemainingEvents()
      }
  }

  @Test
  fun `Login token`() = runTest {
    viewModel.token.test {
      assertNull(awaitItem())

      loginPrefs.token.set(LoginToken(value = "abc123"))
      assertNotNull(awaitItem())

      loginPrefs.token.delete()
      assertNull(awaitItem())

      expectNoEvents()
      cancelAndIgnoreRemainingEvents()
    }
  }
}
