@file:OptIn(ExperimentalCoroutinesApi::class)

package actual.core.connection

import actual.api.client.ActualApisStateHolder
import actual.core.model.ServerUrl
import actual.prefs.AppGlobalPreferences
import actual.test.TestClientFactory
import actual.test.buildPreferences
import actual.test.emptyMockEngine
import alakazam.test.core.MainDispatcherRule
import app.cash.turbine.test
import io.ktor.client.engine.mock.MockEngine
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import okio.FileSystem
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@RunWith(RobolectricTestRunner::class)
class ConnectionMonitorTest {
  @get:Rule val mainDispatcherRule = MainDispatcherRule()

  private lateinit var connectionMonitor: ConnectionMonitor
  private lateinit var preferences: AppGlobalPreferences
  private lateinit var apiStateHolder: ActualApisStateHolder
  private lateinit var mockEngine: MockEngine.Queue
  private lateinit var fileSystem: FileSystem

  private fun TestScope.before() {
    val prefs = buildPreferences(mainDispatcherRule.dispatcher)
    preferences = AppGlobalPreferences(prefs)
    apiStateHolder = ActualApisStateHolder()
    mockEngine = emptyMockEngine()
    fileSystem = FileSystem.SYSTEM

    connectionMonitor = ConnectionMonitor(
      scope = backgroundScope,
      clientFactory = TestClientFactory(mockEngine),
      apiStateHolder = apiStateHolder,
      preferences = preferences,
      fileSystem = fileSystem,
    )
  }

  @AfterTest
  fun after() {
    mockEngine.close()
  }

  @Test
  fun `Keeping no URL does nothing`() = runTest {
    before()

    apiStateHolder.test {
      // Given no APIs are set
      assertNull(awaitItem())

      // and no URL is set
      assertNull(preferences.serverUrl.get())

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
      assertNull(awaitItem())

      // When the URL is set
      preferences.serverUrl.set(ServerUrl.Demo)

      // An API is built and emitted
      assertNotNull(awaitItem())
      advanceUntilIdle()

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
      assertNull(awaitItem())

      // when the monitor starts
      connectionMonitor.start()

      // then an API is built and emitted
      assertNotNull(awaitItem())

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
