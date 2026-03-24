package aktual.app.nav

import aktual.core.model.Protocol
import aktual.core.model.ServerUrl
import aktual.core.model.Token
import aktual.core.prefs.AppPreferences
import aktual.core.prefs.AppPreferencesImpl
import aktual.test.assertThatNextEmission
import aktual.test.assertThatNextEmissionIsEqualTo
import aktual.test.buildPreferences
import app.cash.turbine.test
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class InitialRouteUseCaseTest {
  private lateinit var preferences: AppPreferences
  private lateinit var useCase: InitialRouteUseCase

  private fun runUseCaseTest(testBody: suspend TestScope.() -> Unit): TestResult =
    runTest(timeout = 5.seconds) {
      preferences = AppPreferencesImpl(buildPreferences())
      useCase = InitialRouteUseCase(preferences)
      testBody()
    }

  @Test
  fun `Initial emission is null while loading`() = runUseCaseTest {
    useCase(backgroundScope).test {
      assertThatNextEmission().isNull()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Returns ServerUrlNavRoute when no server URL set`() = runUseCaseTest {
    useCase(backgroundScope).test {
      assertThatNextEmission().isNull()
      assertThatNextEmissionIsEqualTo(ServerUrlNavRoute)
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Returns LoginNavRoute when server URL set but no token`() = runUseCaseTest {
    preferences.serverUrl.set(TEST_SERVER_URL)
    useCase(backgroundScope).test {
      assertThatNextEmission().isNull()
      assertThatNextEmissionIsEqualTo(LoginNavRoute)
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Returns ListBudgetsNavRoute when server URL and token are set`() = runUseCaseTest {
    preferences.serverUrl.set(TEST_SERVER_URL)
    preferences.token.set(TEST_TOKEN)
    useCase(backgroundScope).test {
      assertThatNextEmission().isNull()
      assertThatNextEmission().isEqualTo(ListBudgetsNavRoute(TEST_TOKEN))
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Updates route when token is set`() = runUseCaseTest {
    preferences.serverUrl.set(TEST_SERVER_URL)
    useCase(backgroundScope).test {
      assertThatNextEmission().isNull()
      assertThatNextEmissionIsEqualTo(LoginNavRoute)
      preferences.token.set(TEST_TOKEN)
      assertThatNextEmission().isEqualTo(ListBudgetsNavRoute(TEST_TOKEN))
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Updates route when server URL is set`() = runUseCaseTest {
    useCase(backgroundScope).test {
      assertThatNextEmission().isNull()
      assertThatNextEmissionIsEqualTo(ServerUrlNavRoute)
      preferences.serverUrl.set(TEST_SERVER_URL)
      assertThatNextEmissionIsEqualTo(LoginNavRoute)
      cancelAndIgnoreRemainingEvents()
    }
  }

  private companion object {
    val TEST_SERVER_URL = ServerUrl(Protocol.Https, "example.com")
    val TEST_TOKEN = Token("test-token")
  }
}
