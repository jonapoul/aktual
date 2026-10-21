package aktual.app.nav

import aktual.di.AppCoroutineScope
import aktual.prefs.AppLockTimeout
import aktual.prefs.SystemUiPreferences
import aktual.prefs.SystemUiPreferencesImpl
import aktual.test.assertThatNextEmissionIsEqualTo
import aktual.test.buildPreferences
import alakazam.test.TestClock
import alakazam.test.unconfinedDispatcher
import app.cash.turbine.test
import kotlin.test.Test
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AppLockManagerTest {
  private lateinit var preferences: SystemUiPreferences
  private var now = Instant.fromEpochMilliseconds(0)

  private fun runManagerTest(
    enabled: Boolean,
    timeout: AppLockTimeout = Default,
    testBody: suspend TestScope.(AppLockManager) -> Unit,
  ): TestResult = runTest {
    preferences = SystemUiPreferencesImpl(buildPreferences(unconfinedDispatcher))
    preferences.appLockEnabled.set(enabled)
    preferences.appLockTimeout.set(timeout)
    val scope =
      AppCoroutineScope(CoroutineScope(backgroundScope.coroutineContext + unconfinedDispatcher))
    val manager = AppLockManager(preferences, TestClock { now }, scope)
    testBody(manager)
  }

  private fun AppLockManager.backgroundFor(duration: Duration) {
    onBackground()
    now += duration
    onForeground()
  }

  @Test
  fun `Starts locked when enabled`() =
    runManagerTest(enabled = true) { manager ->
      manager.state.test {
        assertThatNextEmissionIsEqualTo(Locked)
        cancelAndIgnoreRemainingEvents()
      }
    }

  @Test
  fun `Starts unlocked when disabled`() =
    runManagerTest(enabled = false) { manager ->
      manager.state.test {
        assertThatNextEmissionIsEqualTo(Unlocked)
        cancelAndIgnoreRemainingEvents()
      }
    }

  @Test
  fun `Unlocks after authenticating`() =
    runManagerTest(enabled = true) { manager ->
      manager.state.test {
        assertThatNextEmissionIsEqualTo(Locked)
        manager.onAuthenticationStarted()
        manager.onAuthenticated()
        assertThatNextEmissionIsEqualTo(Unlocked)
        cancelAndIgnoreRemainingEvents()
      }
    }

  @Test
  fun `Stays locked after failed authentication`() =
    runManagerTest(enabled = true) { manager ->
      manager.state.test {
        assertThatNextEmissionIsEqualTo(Locked)
        manager.onAuthenticationStarted()
        manager.onAuthenticationFailed()
        expectNoEvents()
      }
    }

  @Test
  fun `Locks immediately on foreground`() =
    runManagerTest(enabled = true) { manager ->
      manager.state.test {
        assertThatNextEmissionIsEqualTo(Locked)
        manager.onAuthenticated()
        assertThatNextEmissionIsEqualTo(Unlocked)

        manager.backgroundFor(Duration.ZERO)
        assertThatNextEmissionIsEqualTo(Locked)
        cancelAndIgnoreRemainingEvents()
      }
    }

  @Test
  fun `Locks only after the timeout`() =
    runManagerTest(enabled = true, timeout = Seconds30) { manager ->
      manager.state.test {
        assertThatNextEmissionIsEqualTo(Locked)
        manager.onAuthenticated()
        assertThatNextEmissionIsEqualTo(Unlocked)

        // Given back before the timeout, stays unlocked
        manager.backgroundFor(29.seconds)
        expectNoEvents()

        // When away for the full timeout, locks
        manager.backgroundFor(30.seconds)
        assertThatNextEmissionIsEqualTo(Locked)
        cancelAndIgnoreRemainingEvents()
      }
    }

  @Test
  fun `Never locks on foreground when disabled`() =
    runManagerTest(enabled = false) { manager ->
      manager.state.test {
        assertThatNextEmissionIsEqualTo(Unlocked)
        manager.backgroundFor(1.seconds)
        expectNoEvents()
      }
    }

  @Test
  fun `Foreground without a background does nothing`() =
    runManagerTest(enabled = true) { manager ->
      manager.state.test {
        assertThatNextEmissionIsEqualTo(Locked)
        manager.onAuthenticated()
        assertThatNextEmissionIsEqualTo(Unlocked)
        manager.onForeground()
        expectNoEvents()
      }
    }

  @Test
  fun `Ignores lifecycle events while authenticating`() =
    runManagerTest(enabled = true) { manager ->
      manager.state.test {
        assertThatNextEmissionIsEqualTo(Locked)
        manager.onAuthenticated()
        assertThatNextEmissionIsEqualTo(Unlocked)

        // The prompt backgrounds the app, then succeeds after we're foregrounded again
        manager.onAuthenticationStarted()
        manager.backgroundFor(1.seconds)
        manager.onAuthenticated()
        expectNoEvents()

        // Normal lifecycle events lock again afterwards
        manager.backgroundFor(1.seconds)
        assertThatNextEmissionIsEqualTo(Locked)
        cancelAndIgnoreRemainingEvents()
      }
    }

  @Test
  fun `Disabling unlocks`() =
    runManagerTest(enabled = true) { manager ->
      manager.state.test {
        assertThatNextEmissionIsEqualTo(Locked)
        preferences.appLockEnabled.set(false)
        assertThatNextEmissionIsEqualTo(Unlocked)
        cancelAndIgnoreRemainingEvents()
      }
    }

  @Test
  fun `Enabling doesn't lock until the next foreground`() =
    runManagerTest(enabled = false) { manager ->
      manager.state.test {
        assertThatNextEmissionIsEqualTo(Unlocked)
        preferences.appLockEnabled.set(true)
        expectNoEvents()

        manager.backgroundFor(1.seconds)
        assertThatNextEmissionIsEqualTo(Locked)
        cancelAndIgnoreRemainingEvents()
      }
    }
}
