package aktual.app.nav

import aktual.di.AppCoroutineScope
import aktual.prefs.AppLockTimeout
import aktual.prefs.SecurityPreferences
import aktual.prefs.SecurityPreferencesImpl
import aktual.test.assertThatNextEmissionIsEqualTo
import aktual.test.buildPreferences
import alakazam.test.TestClock
import alakazam.test.unconfinedDispatcher
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isFalse
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
  private lateinit var preferences: SecurityPreferences
  private var now = Instant.fromEpochMilliseconds(0)

  private fun runManagerTest(
    enabled: Boolean,
    timeout: AppLockTimeout = Default,
    testBody: suspend TestScope.(AppLockManager) -> Unit,
  ): TestResult = runTest {
    preferences = SecurityPreferencesImpl(buildPreferences(unconfinedDispatcher))
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
  fun `Handles lifecycle events again after a cancelled prompt`() =
    runManagerTest(enabled = true, timeout = Seconds30) { manager ->
      manager.state.test {
        assertThatNextEmissionIsEqualTo(Locked)
        manager.onAuthenticated()
        assertThatNextEmissionIsEqualTo(Unlocked)

        manager.onAuthenticationStarted()
        manager.onAuthenticationFailed()
        manager.backgroundFor(30.seconds)
        assertThatNextEmissionIsEqualTo(Locked)
        cancelAndIgnoreRemainingEvents()
      }
    }

  @Test
  fun `Locks when the clock moves backwards`() =
    runManagerTest(enabled = true, timeout = Minutes15) { manager ->
      manager.state.test {
        assertThatNextEmissionIsEqualTo(Locked)
        manager.onAuthenticated()
        assertThatNextEmissionIsEqualTo(Unlocked)

        manager.backgroundFor((-1).seconds)
        assertThatNextEmissionIsEqualTo(Locked)
        cancelAndIgnoreRemainingEvents()
      }
    }

  @Test
  fun `Locks immediately on foreground`() =
    runManagerTest(enabled = true) { manager ->
      manager.state.test {
        assertThatNextEmissionIsEqualTo(Locked)
        manager.onAuthenticated()
        assertThatNextEmissionIsEqualTo(Unlocked)

        manager.backgroundFor(ZERO)
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
  fun `Disabling from the manager unlocks and clears the pref`() =
    runManagerTest(enabled = true) { manager ->
      manager.state.test {
        assertThatNextEmissionIsEqualTo(Locked)
        manager.disable()
        assertThatNextEmissionIsEqualTo(Unlocked)
        assertThat(preferences.appLockEnabled.get()).isFalse()
        cancelAndIgnoreRemainingEvents()
      }
    }

  @Test
  fun `Unavailable stays locked until the user has seen why`() =
    runManagerTest(enabled = true) { manager ->
      manager.state.test {
        assertThatNextEmissionIsEqualTo(Locked)
        manager.onUnavailable()
        expectNoEvents()

        manager.disable()
        assertThatNextEmissionIsEqualTo(Unlocked)
        cancelAndIgnoreRemainingEvents()
      }
    }

  @Test
  fun `Unavailable flag is cleared on disable`() =
    runManagerTest(enabled = true) { manager ->
      manager.isUnavailable.test {
        assertThatNextEmissionIsEqualTo(false)
        manager.onUnavailable()
        assertThatNextEmissionIsEqualTo(true)
        manager.disable()
        assertThatNextEmissionIsEqualTo(false)
        cancelAndIgnoreRemainingEvents()
      }
    }

  @Test
  fun `Disabling clears an unfinished prompt`() =
    runManagerTest(enabled = true) { manager ->
      manager.state.test {
        assertThatNextEmissionIsEqualTo(Locked)
        // The prompt never reports back
        manager.onAuthenticationStarted()
        preferences.appLockEnabled.set(false)
        assertThatNextEmissionIsEqualTo(Unlocked)

        preferences.appLockEnabled.set(true)
        manager.backgroundFor(1.seconds)
        assertThatNextEmissionIsEqualTo(Locked)
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
