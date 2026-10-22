package aktual.prefs

import aktual.test.assertThatNextEmissionIsEqualTo
import aktual.test.buildPreferences
import alakazam.test.unconfinedDispatcher
import app.cash.turbine.test
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SecurityPreferencesTest {
  @Test
  fun `App lock enabled`() = runTest {
    val preferences = SecurityPreferencesImpl(buildPreferences(unconfinedDispatcher))
    with(preferences.appLockEnabled) {
      asFlow().test {
        assertThatNextEmissionIsEqualTo(false)
        set(true)
        assertThatNextEmissionIsEqualTo(true)
        cancelAndIgnoreRemainingEvents()
      }
    }
  }

  @Test
  fun `App lock timeout`() = runTest {
    val preferences = SecurityPreferencesImpl(buildPreferences(unconfinedDispatcher))
    with(preferences.appLockTimeout) {
      asFlow().test {
        assertThatNextEmissionIsEqualTo(Immediately)
        set(Minutes5)
        assertThatNextEmissionIsEqualTo(Minutes5)
        cancelAndIgnoreRemainingEvents()
      }
    }
  }
}
