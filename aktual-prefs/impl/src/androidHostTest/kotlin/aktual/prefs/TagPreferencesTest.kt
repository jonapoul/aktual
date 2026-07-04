package aktual.prefs

import aktual.budget.model.TagSort
import aktual.test.assertThatNextEmissionIsEqualTo
import aktual.test.buildPreferences
import alakazam.test.unconfinedDispatcher
import app.cash.turbine.test
import kotlin.test.Test
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TagPreferencesTest {
  private lateinit var preferences: TagPreferences

  private fun TestScope.before() {
    preferences = TagPreferencesImpl(buildPreferences(unconfinedDispatcher))
  }

  @Test
  fun `Last used tag colour`() = runTest {
    before()
    with(preferences.lastUsedTagColor) {
      asFlow().test {
        // Given nothing has been stored yet
        assertThatNextEmissionIsEqualTo(null)

        // When a colour is stored
        set("#AABBCC")

        // Then it round-trips
        assertThatNextEmissionIsEqualTo("#AABBCC")

        // When cleared
        delete()

        // Then it's back to null
        assertThatNextEmissionIsEqualTo(null)
        cancelAndIgnoreRemainingEvents()
      }
    }
  }

  @Test
  fun `Tag sort field`() = runTest {
    before()
    with(preferences.sortField) {
      asFlow().test {
        // Given the default sort field
        assertThatNextEmissionIsEqualTo(TagSort.Field.Default)

        // When a different field is stored
        set(TagSort.Field.Usage)

        // Then it round-trips
        assertThatNextEmissionIsEqualTo(TagSort.Field.Usage)
        cancelAndIgnoreRemainingEvents()
      }
    }
  }

  @Test
  fun `Tag sort direction`() = runTest {
    before()
    with(preferences.sortDirection) {
      asFlow().test {
        // Given the default sort direction
        assertThatNextEmissionIsEqualTo(TagSort.Direction.Default)

        // When a different direction is stored
        set(TagSort.Direction.Descending)

        // Then it round-trips
        assertThatNextEmissionIsEqualTo(TagSort.Direction.Descending)
        cancelAndIgnoreRemainingEvents()
      }
    }
  }
}
