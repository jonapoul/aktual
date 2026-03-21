package aktual.core.connection

import aktual.api.client.AktualApisStateHolder
import aktual.api.client.ApiBuilder
import aktual.core.model.ServerUrl
import aktual.core.prefs.AppGlobalPreferences
import aktual.core.prefs.AppGlobalPreferencesImpl
import aktual.core.prefs.delete
import aktual.test.CoLogcatInterceptor
import aktual.test.assertThatNextEmission
import aktual.test.assertThatNextEmissionIsEqualTo
import aktual.test.buildPreferences
import alakazam.test.TestCoroutineContexts
import alakazam.test.unconfinedDispatcher
import app.cash.burst.InterceptTest
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ConnectionMonitorTest {
  @InterceptTest val logger = CoLogcatInterceptor()

  private lateinit var connectionMonitor: ConnectionMonitor
  private lateinit var preferences: AppGlobalPreferences
  private lateinit var apiStateHolder: AktualApisStateHolder

  private fun TestScope.before() {
    val prefs = buildPreferences(unconfinedDispatcher)
    preferences = AppGlobalPreferencesImpl(prefs)
    apiStateHolder = AktualApisStateHolder()

    val apiBuilder =
      mockk<ApiBuilder> {
        every { account() } returns mockk()
        every { base() } returns mockk()
        every { health() } returns mockk()
        every { metrics() } returns mockk()
        every { sync() } returns mockk()
      }

    connectionMonitor =
      ConnectionMonitorImpl(
        scope = backgroundScope,
        contexts = TestCoroutineContexts(unconfinedDispatcher),
        apiStateHolder = apiStateHolder,
        preferences = preferences,
        apiBuilder = { apiBuilder },
      )
  }

  @Test
  fun `Keeping no URL does nothing`() = runTest {
    before()

    apiStateHolder.test {
      // Given no APIs are set
      assertThatNextEmissionIsEqualTo(null)

      // and no URL is set
      assertThat(preferences.serverUrl.get()).isEqualTo(null)

      // When we start and wait for things to settle
      connectionMonitor.start()
      advanceUntilIdle()

      // No APIs are built
      expectNoEvents()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Build APIs when URL is set`() = runTest {
    before()

    // Given we've started and waited for things to settle
    connectionMonitor.start()
    advanceUntilIdle()

    apiStateHolder.test {
      // Given no APIs are set
      assertThatNextEmissionIsEqualTo(null)

      // When the URL is set
      preferences.serverUrl.set(ServerUrl.Demo)

      // An API is built and emitted
      assertThatNextEmission().isNotNull()
      advanceUntilIdle()

      // and nothing else is done
      expectNoEvents()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Remove APIs when URL is cleared`() = runTest {
    before()

    // Given we've set a URL and started
    preferences.serverUrl.set(ServerUrl.Demo)
    advanceUntilIdle()

    apiStateHolder.test {
      // Nothing initially
      assertThatNextEmissionIsEqualTo(null)

      // when the monitor starts
      connectionMonitor.start()

      // then an API is built and emitted
      assertThat(awaitItem()).isNotNull()

      // When the URL is cleared
      preferences.serverUrl.delete()
      advanceUntilIdle()

      // Then the null API is emitted
      assertThatNextEmissionIsEqualTo(null)

      // and nothing else is done
      expectNoEvents()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Ignore URL changes if stopped`() = runTest {
    before()

    // Given we've set a URL and started
    preferences.serverUrl.set(ServerUrl.Demo)

    apiStateHolder.test {
      // Nothing initially
      assertThatNextEmissionIsEqualTo(null)

      // when the monitor starts
      connectionMonitor.start()

      // then an API is built and emitted
      assertThatNextEmission().isNotNull()

      // When the monitor is stopped
      connectionMonitor.stop()
      advanceUntilIdle()

      // and the URL is cleared
      preferences.serverUrl.delete()

      // nothing else is emitted
      expectNoEvents()
      cancelAndIgnoreRemainingEvents()
    }
  }
}
