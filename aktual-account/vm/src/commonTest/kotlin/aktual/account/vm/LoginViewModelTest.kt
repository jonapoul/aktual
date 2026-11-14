package aktual.account.vm

import aktual.core.model.AktualVersionsStateHolder
import aktual.core.model.AvailableLoginMethod
import aktual.core.model.Token
import aktual.prefs.AppPreferences
import aktual.prefs.AppPreferencesImpl
import aktual.test.TestBuildConfig
import aktual.test.assertThatNextEmission
import aktual.test.buildPreferences
import aktual.test.viewModelContext
import alakazam.test.unconfinedDispatcher
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isTrue
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest

internal class LoginViewModelTest {
  // real
  private lateinit var preferences: AppPreferences
  private lateinit var viewModel: LoginViewModel
  private lateinit var versionsStateHolder: AktualVersionsStateHolder

  // mock
  private lateinit var loginRequester: LoginRequester

  private fun runLoginTest(
    configure: LoginRequester.() -> Unit = {},
    action: suspend TestScope.() -> Unit,
  ) = runTest {
    val prefs = buildPreferences(unconfinedDispatcher)
    preferences = AppPreferencesImpl(prefs)
    versionsStateHolder = AktualVersionsStateHolder(TestBuildConfig)
    loginRequester = mockk<LoginRequester>(relaxed = true).apply(configure)
    viewModel =
      LoginViewModel(
        versionsStateHolder = versionsStateHolder,
        loginRequester = loginRequester,
        preferences = preferences,
        buildConfig = TestBuildConfig.copy(defaultPassword = Empty),
        viewModelContext = viewModelContext,
      )
    action()
  }

  @Test
  fun `Enter password`() = runLoginTest {
    viewModel.enteredPassword.test {
      assertThatNextEmission().transform { it.value }.isEmpty()

      viewModel.onEnterPassword(password = "hello world")
      assertThat(awaitItem().value).isEqualTo("hello world")

      viewModel.clearState()
      assertThat(awaitItem().value).isEmpty()

      expectNoEvents()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Shows a failure when the OpenID token is rejected`() =
    runLoginTest(
      configure = {
        every { openIdTokens } returns flowOf(OPENID_TOKEN)
        coEvery { logIn(OPENID_TOKEN) } returns TokenExpired
      },
    ) {
      viewModel.loginFailure.test {
        assertThat(awaitItem()).isNull()
        assertThat(awaitItem()).isEqualTo(TokenExpired)
      }
      coVerify { loginRequester.logIn(OPENID_TOKEN) }
    }

  @Test
  fun `Signing in with invalid password`() = runLoginTest {
    combine(viewModel.loginFailure, viewModel.isLoading, ::Pair).distinctUntilChanged().test {
      // Initially not loading or failed
      val (failure1, loading1) = awaitItem()
      assertThat(failure1).isNull()
      assertThat(loading1).isFalse()

      // When we make the failing request
      coEvery { loginRequester.logIn(any(), any()) } returns InvalidPassword
      viewModel.onEnterPassword("password")
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
      assertThat(failure4).isEqualTo(InvalidPassword)
      assertThat(loading4).isFalse()

      expectNoEvents()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Signing in with token expired`() = runLoginTest {
    combine(viewModel.loginFailure, viewModel.isLoading, ::Pair).distinctUntilChanged().test {
      // Initially not loading or failed
      val (failure1, loading1) = awaitItem()
      assertThat(failure1).isNull()
      assertThat(loading1).isFalse()

      // When we make the failing request
      coEvery { loginRequester.logIn(any(), any()) } returns TokenExpired
      viewModel.onEnterPassword("password")
      viewModel.onClickSignIn()

      // Then we're loading but not failed
      val (failure2, loading2) = awaitItem()
      assertThat(failure2).isNull()
      assertThat(loading2).isTrue()

      // Then not loading
      val (failure3, loading3) = awaitItem()
      assertThat(failure3).isNull()
      assertThat(loading3).isFalse()

      // Then failed with token expired
      val (failure4, loading4) = awaitItem()
      assertThat(failure4).isEqualTo(TokenExpired)
      assertThat(loading4).isFalse()

      expectNoEvents()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Unknown login methods are filtered out`() = runLoginTest {
    coEvery { loginRequester.fetchLoginMethods() } returns
      listOf(
        AvailableLoginMethod(method = Unknown, displayName = "SAML", isActive = true),
        AvailableLoginMethod(method = Header, displayName = "Header", isActive = true),
      )

    viewModel.loginMethods.test {
      assertThat(awaitItem()).isEmpty()
      assertThat(awaitItem()).isEqualTo(persistentListOf(Header, ApiKey))
      cancelAndIgnoreRemainingEvents()
    }

    viewModel.selectedLoginMethod.test {
      assertThat(awaitItem()).isEqualTo(Header)
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `API key is offered alongside password when the server lists no methods`() = runLoginTest {
    coEvery { loginRequester.fetchLoginMethods() } returns emptyList()

    viewModel.loginMethods.test {
      assertThat(awaitItem()).isEmpty()
      assertThat(awaitItem()).isEqualTo(persistentListOf(Password, ApiKey))
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Signing in with an API key`() = runLoginTest {
    coEvery { loginRequester.logIn(any<Token>()) } returns InvalidApiKey

    viewModel.loginFailure.test {
      assertThat(awaitItem()).isNull()

      viewModel.onSelectLoginMethod(ApiKey)
      viewModel.onEnterApiKey("  actual_abc123 ")
      viewModel.onClickSignIn()

      assertThat(awaitItem()).isEqualTo(InvalidApiKey)
      coVerify { loginRequester.logIn(Token("actual_abc123")) }
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Signing in with an empty API key does nothing`() = runLoginTest {
    viewModel.onSelectLoginMethod(ApiKey)
    viewModel.onClickSignIn()
    coVerify(exactly = 0) { loginRequester.logIn(any<Token>()) }
  }

  @Test
  fun `Login token`() = runLoginTest {
    viewModel.token.test {
      expectNoEvents()

      preferences.token.set(Token(value = "abc123"))
      assertThatNextEmission().isNotNull()

      expectNoEvents()
      cancelAndIgnoreRemainingEvents()
    }
  }

  private companion object {
    val OPENID_TOKEN = Token("openid-session")
  }
}
