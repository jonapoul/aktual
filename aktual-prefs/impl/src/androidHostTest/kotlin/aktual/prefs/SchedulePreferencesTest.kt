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
class SchedulePreferencesTest {
  @Test
  fun `Show completed schedules`() = runTest {
    val preferences = SchedulePreferencesImpl(buildPreferences(unconfinedDispatcher))
    with(preferences.showCompleted) {
      asFlow().test {
        // Given nothing has been stored yet, completed schedules are hidden
        assertThatNextEmissionIsEqualTo(false)

        // When enabled
        set(true)

        // Then it round-trips
        assertThatNextEmissionIsEqualTo(true)
        cancelAndIgnoreRemainingEvents()
      }
    }
  }
}
