package aktual.prefs

import aktual.test.assertThatNextEmissionIsEqualTo
import aktual.test.buildPreferences
import alakazam.test.unconfinedDispatcher
import app.cash.turbine.test
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class TransactionPreferencesTest {
  @Test
  fun `Alternate row colours`() = runTest {
    val preferences = TransactionPreferencesImpl(buildPreferences(unconfinedDispatcher))
    with(preferences.alternateRowColours) {
      asFlow().test {
        // Given nothing has been stored yet, rows aren't alternated
        assertThatNextEmissionIsEqualTo(false)

        // When enabled
        set(true)

        // Then it round-trips
        assertThatNextEmissionIsEqualTo(true)
        cancelAndIgnoreRemainingEvents()
      }
    }
  }

  @Test
  fun `Expand splits`() = runTest {
    val preferences = TransactionPreferencesImpl(buildPreferences(unconfinedDispatcher))
    with(preferences.expandSplits) {
      asFlow().test {
        // Given nothing has been stored yet, splits start collapsed
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
