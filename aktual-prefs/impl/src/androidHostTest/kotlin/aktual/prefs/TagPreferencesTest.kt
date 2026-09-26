package aktual.prefs

import aktual.test.assertThatNextEmissionIsEqualTo
import aktual.test.buildPreferences
import alakazam.test.unconfinedDispatcher
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import app.cash.turbine.test
import kotlin.test.Test
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TagPreferencesTest {
  private lateinit var dataStore: DataStore<Preferences>
  private lateinit var preferences: TagPreferences

  private fun TestScope.before() {
    dataStore = buildPreferences(unconfinedDispatcher)
    preferences = TagPreferencesImpl(dataStore)
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
        assertThatNextEmissionIsEqualTo(Default)

        // When a different field is stored
        set(Usage)

        // Then it round-trips
        assertThatNextEmissionIsEqualTo(Usage)
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
        assertThatNextEmissionIsEqualTo(Default)

        // When a different direction is stored
        set(Descending)

        // Then it round-trips
        assertThatNextEmissionIsEqualTo(Descending)
        cancelAndIgnoreRemainingEvents()
      }
    }
  }

  @Test
  fun `Unknown stored sort field falls back to default`() = runTest {
    before()
    with(preferences.sortField) {
      asFlow().test {
        assertThatNextEmissionIsEqualTo(Default)

        // When a value this app version doesn't know is stored
        set(Usage)
        assertThatNextEmissionIsEqualTo(Usage)
        dataStore.edit { it[stringPreferencesKey("tagSortField")] = "something-new" }

        // Then it decodes as the default instead of throwing
        assertThatNextEmissionIsEqualTo(Default)
        cancelAndIgnoreRemainingEvents()
      }
    }
  }
}
