@file:OptIn(ExperimentalCoroutinesApi::class)

package actual.core.connection

import actual.api.client.ActualApisStateHolder
import actual.core.model.ServerUrl
import actual.prefs.ServerUrlPreferences
import actual.test.TestClientFactory
import actual.test.buildPreferences
import actual.test.emptyMockEngine
import alakazam.test.core.Flaky
import alakazam.test.core.FlakyTestRule
import alakazam.test.core.MainDispatcherRule
import app.cash.turbine.test
import io.ktor.client.engine.mock.MockEngine
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import okio.fakefilesystem.FakeFileSystem
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.seconds

@RunWith(RobolectricTestRunner::class)
class ConnectionMonitorTest {
  @get:Rule val mainDispatcherRule = MainDispatcherRule()
  @get:Rule val flakyTestRule = FlakyTestRule()

  private lateinit var connectionMonitor: ConnectionMonitor
  private lateinit var serverUrlPreferences: ServerUrlPreferences
  private lateinit var apiStateHolder: ActualApisStateHolder
  private lateinit var mockEngine: MockEngine
  private lateinit var fileSystem: FakeFileSystem

  private fun TestScope.before() {
    val prefs = buildPreferences(mainDispatcherRule.dispatcher)
    serverUrlPreferences = ServerUrlPreferences(prefs)
    apiStateHolder = ActualApisStateHolder()
    mockEngine = emptyMockEngine()
    fileSystem = FakeFileSystem()

    connectionMonitor = ConnectionMonitor(
      scope = backgroundScope,
      clientFactory = TestClientFactory(mockEngine),
      apiStateHolder = apiStateHolder,
      serverUrlPreferences = serverUrlPreferences,
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
      assertNull(serverUrlPreferences.url.get())

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
      serverUrlPreferences.url.set(ServerUrl.Demo)

      // An API is built and emitted
      assertNotNull(awaitItem())
      advanceUntilIdle()

      // and nothing else is done
      expectNoEvents()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  @Flaky(retry = 5, reason = "Sometimes times out on assertNotNull after starting")
  fun `Remove APIs when URL is cleared`() = runTest(timeout = 10.seconds) {
    before()

    // Given we've set a URL and started
    serverUrlPreferences.url.set(ServerUrl.Demo)

    apiStateHolder.test {
      // Nothing initially
      assertNull(awaitItem())

      // when the monitor starts
      connectionMonitor.start()

      // then an API is built and emitted
      assertNotNull(awaitItem())

      // When the URL is cleared
      serverUrlPreferences.url.delete()

      // Then the null API is emitted
      assertNull(awaitItem())

      // and nothing else is done
      expectNoEvents()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Ignore URL changes if stopped`() = runTest {
    before()

    // Given we've set a URL and started
    serverUrlPreferences.url.set(ServerUrl.Demo)

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
      serverUrlPreferences.url.delete()

      // nothing else is emitted
      expectNoEvents()
      cancelAndIgnoreRemainingEvents()
    }
  }
}
