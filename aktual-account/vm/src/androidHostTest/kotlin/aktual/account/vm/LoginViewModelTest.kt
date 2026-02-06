package aktual.account.vm

import aktual.account.domain.LoginRequester
import aktual.account.domain.LoginResult
import aktual.core.model.AktualVersionsStateHolder
import aktual.core.model.Password
import aktual.core.model.Protocol
import aktual.core.model.ServerUrl
import aktual.core.model.Token
import aktual.core.prefs.AppGlobalPreferences
import aktual.test.TestBuildConfig
import aktual.test.assertThatNextEmission
import aktual.test.buildPreferences
import alakazam.test.standardDispatcher
import alakazam.test.unconfinedDispatcher
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isTrue
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
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

@RunWith(RobolectricTestRunner::class)
internal class LoginViewModelTest {
  // real
  private lateinit var preferences: AppGlobalPreferences
  private lateinit var viewModel: LoginViewModel
  private lateinit var versionsStateHolder: AktualVersionsStateHolder

  // mock
  private lateinit var loginRequester: LoginRequester

  private fun TestScope.before() {
    Dispatchers.setMain(standardDispatcher)
    val prefs = buildPreferences(unconfinedDispatcher)
    preferences = AppGlobalPreferences(prefs)
    versionsStateHolder = AktualVersionsStateHolder(TestBuildConfig)
    loginRequester = mockk(relaxed = true)
    viewModel = LoginViewModel(
      versionsStateHolder = versionsStateHolder,
      loginRequester = loginRequester,
      preferences = preferences,
      buildConfig = TestBuildConfig.copy(defaultPassword = Password.Empty),
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
      assertThatNextEmission().transform { it.value }.isEqualTo("")

      viewModel.onEnterPassword(password = "hello world")
      assertThat(awaitItem().value).isEqualTo("hello world")

      viewModel.clearState()
      assertThat(awaitItem().value).isEqualTo("")

      expectNoEvents()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Server URL`() = runTest {
    before()
    viewModel.serverUrl.test {
      assertThatNextEmission().isNull()

      val url = ServerUrl(Protocol.Https, baseUrl = "url.for.my.server.com")
      preferences.serverUrl.set(url)

      assertThat(awaitItem()).isEqualTo(url)
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
        assertThat(failure1).isNull()
        assertThat(loading1).isFalse()

        // When we make the failing request
        coEvery { loginRequester.logIn(any()) } returns LoginResult.InvalidPassword
        viewModel.onClickSignIn()

        // Then we're loading but not failed
        val (failure2, loading2) = awaitItem()
        assertThat(failure2).isNull()
        assertThat(loading2).isTrue()

        // Then not loading
        val (failure3, loading3) = awaitItem()
        assertThat(failure3).isNull()
        assertThat(loading3).isFalse()

        // Then failed
        val (failure4, loading4) = awaitItem()
        assertThat(failure4).isEqualTo(LoginResult.InvalidPassword)
        assertThat(loading4).isFalse()

        expectNoEvents()
        cancelAndIgnoreRemainingEvents()
      }
  }

  @Test
  fun `Login token`() = runTest {
    before()
    viewModel.token.test {
      expectNoEvents()

      preferences.token.set(Token(value = "abc123"))
      assertThatNextEmission().isNotNull()

      expectNoEvents()
      cancelAndIgnoreRemainingEvents()
    }
  }
}
