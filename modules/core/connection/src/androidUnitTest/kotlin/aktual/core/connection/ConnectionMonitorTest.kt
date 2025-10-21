/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
@file:OptIn(ExperimentalCoroutinesApi::class)

package aktual.core.connection

import aktual.api.client.AktualApisStateHolder
import aktual.core.model.ServerUrl
import aktual.prefs.AppGlobalPreferences
import aktual.test.CoLogcatInterceptor
import aktual.test.TestClientFactory
import aktual.test.assertThatNextEmission
import aktual.test.assertThatNextEmissionIsEqualTo
import aktual.test.buildPreferences
import aktual.test.emptyMockEngine
import alakazam.test.core.TestCoroutineContexts
import alakazam.test.core.unconfinedDispatcher
import app.cash.burst.InterceptTest
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import io.ktor.client.engine.mock.MockEngine
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import okio.FileSystem
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.AfterTest
import kotlin.test.Test

@RunWith(RobolectricTestRunner::class)
class ConnectionMonitorTest {
  @InterceptTest val logger = CoLogcatInterceptor()

  private lateinit var connectionMonitor: ConnectionMonitor
  private lateinit var preferences: AppGlobalPreferences
  private lateinit var apiStateHolder: AktualApisStateHolder
  private lateinit var mockEngine: MockEngine.Queue
  private lateinit var fileSystem: FileSystem

  private fun TestScope.before() {
    val prefs = buildPreferences(unconfinedDispatcher)
    preferences = AppGlobalPreferences(prefs)
    apiStateHolder = AktualApisStateHolder()
    mockEngine = emptyMockEngine()
    fileSystem = FileSystem.SYSTEM

    connectionMonitor = ConnectionMonitor(
      scope = backgroundScope,
      contexts = TestCoroutineContexts(unconfinedDispatcher),
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
