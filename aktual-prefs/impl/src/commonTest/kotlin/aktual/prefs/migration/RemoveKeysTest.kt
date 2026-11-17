package aktual.prefs.migration

import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.preferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class RemoveKeysTest {
  private val removed = stringPreferencesKey("removed")
  private val kept = intPreferencesKey("kept")
  private val migration = RemoveKeys(listOf(removed))

  @Test
  fun `Removes listed keys and keeps the rest`() = runTest {
    val prefs = preferencesOf(removed to "x", kept to 1)
    assertThat(migration.shouldMigrate(prefs)).isTrue()
    assertThat(migration.migrate(prefs)).isEqualTo(preferencesOf(kept to 1))
  }

  @Test
  fun `Matches keys by name whatever their type`() = runTest {
    val prefs = preferencesOf(intPreferencesKey("removed") to 3)
    assertThat(migration.migrate(prefs)).isEqualTo(preferencesOf())
  }

  @Test
  fun `Skips when no listed key is stored`() = runTest {
    assertThat(migration.shouldMigrate(preferencesOf(kept to 1))).isFalse()
  }
}
