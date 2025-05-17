package actual.account.login.vm

import actual.account.login.domain.LoginPreferences
import actual.account.login.domain.LoginRequester
import actual.account.login.domain.LoginResult
import actual.account.model.LoginToken
import actual.account.model.Password
import actual.core.versions.ActualVersionsStateHolder
import actual.test.TestBuildConfig
import actual.test.buildPreferences
import actual.url.model.Protocol
import actual.url.model.ServerUrl
import actual.url.prefs.ServerUrlPreferences
import alakazam.test.core.standardDispatcher
import alakazam.test.core.unconfinedDispatcher
import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
internal class LoginViewModelTest {
  // real
  private lateinit var serverUrlPrefs: ServerUrlPreferences
  private lateinit var loginPrefs: LoginPreferences
  private lateinit var viewModel: LoginViewModel
  private lateinit var versionsStateHolder: ActualVersionsStateHolder

  // mock
  private lateinit var loginRequester: LoginRequester

  private fun TestScope.before() {
    Dispatchers.setMain(standardDispatcher)
    val prefs = buildPreferences(unconfinedDispatcher)
    serverUrlPrefs = ServerUrlPreferences(prefs)
    loginPrefs = LoginPreferences(prefs)
    versionsStateHolder = ActualVersionsStateHolder(TestBuildConfig)
    loginRequester = mockk(relaxed = true)
    viewModel = LoginViewModel(
      versionsStateHolder = versionsStateHolder,
      loginRequester = loginRequester,
      serverUrlPrefs = serverUrlPrefs,
      loginPrefs = loginPrefs,
      passwordProvider = { Password.Empty },
    )
  }

  @AfterTest
  fun after() {
    Dispatchers.resetMain()
  }

  @Test
  fun `Enter password`() = runTest {
    before()
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
    before()
    viewModel.serverUrl.test {
      assertNull(awaitItem())

      val url = ServerUrl(Protocol.Https, baseUrl = "url.for.my.server.com")
      serverUrlPrefs.url.set(url)

      assertEquals(expected = url, actual = awaitItem())
      expectNoEvents()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Signing in with invalid password`() = runTest {
    before()
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
    before()
    viewModel.token.test {
      expectNoEvents()

      loginPrefs.token.set(LoginToken(value = "abc123"))
      assertNotNull(awaitItem())

      loginPrefs.token.delete()
      expectNoEvents()
      cancelAndIgnoreRemainingEvents()
    }
  }
}
