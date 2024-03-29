package dev.jonpoulton.actual.core.connection

import alakazam.test.core.CoroutineRule
import app.cash.turbine.test
import dev.jonpoulton.actual.api.client.ActualApisStateHolder
import dev.jonpoulton.actual.core.model.ServerUrl
import dev.jonpoulton.actual.serverurl.prefs.ServerUrlPreferences
import dev.jonpoulton.actual.test.buildFlowPreferences
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@RunWith(RobolectricTestRunner::class)
class ConnectionMonitorTest {
  @get:Rule
  val coroutineRule = CoroutineRule()

  private lateinit var connectionMonitor: ConnectionMonitor
  private lateinit var serverUrlPreferences: ServerUrlPreferences
  private lateinit var apiStateHolder: ActualApisStateHolder

  @Before
  fun before() {
    val prefs = buildFlowPreferences(coroutineRule.dispatcher)
    serverUrlPreferences = ServerUrlPreferences(prefs)
    apiStateHolder = ActualApisStateHolder()

    connectionMonitor = ConnectionMonitor(
      scope = coroutineRule.scope,
      apiStateHolder = apiStateHolder,
      serverUrlPreferences = serverUrlPreferences,
    )
  }

  @Test
  fun `Keeping no URL does nothing`() = runTest {
    apiStateHolder.state.test {
      // Given no APIs are set
      assertNull(awaitItem())

      // and no URL is set
      assertNull(serverUrlPreferences.url.get())

      // When we start and wait for things to settle
      connectionMonitor.start()
      coroutineRule.advanceUntilIdle()

      // No APIs are built
      expectNoEvents()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Build APIs when URL is set`() = runTest {
    // Given we've started and waited for things to settle
    connectionMonitor.start()
    coroutineRule.advanceUntilIdle()

    apiStateHolder.state.test {
      // Given no APIs are set
      assertNull(awaitItem())

      // When the URL is set
      serverUrlPreferences.url.set(ServerUrl.Demo)

      // An API is built and emitted
      assertNotNull(awaitItem())
      coroutineRule.advanceUntilIdle()

      // and nothing else is done
      expectNoEvents()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Remove APIs when URL is cleared`() = runTest {
    // Given we've set a URL and started
    serverUrlPreferences.url.set(ServerUrl.Demo)
    connectionMonitor.start()
    coroutineRule.advanceUntilIdle()

    apiStateHolder.state.test {
      // and an API is built and emitted
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
    // Given we've set a URL and started
    serverUrlPreferences.url.set(ServerUrl.Demo)
    connectionMonitor.start()
    coroutineRule.advanceUntilIdle()

    apiStateHolder.state.test {
      // and an API is built and emitted
      assertNotNull(awaitItem())

      // When the monitor is stopped
      connectionMonitor.stop()
      coroutineRule.advanceUntilIdle()

      // and the URL is cleared
      serverUrlPreferences.url.delete()

      // nothing else is emitted
      expectNoEvents()
      cancelAndIgnoreRemainingEvents()
    }
  }
}
