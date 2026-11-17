package aktual.prefs.migration

import androidx.datastore.core.DataMigration
import androidx.datastore.preferences.core.Preferences

// Deletes stored values for keys that aren't used any more. Keys match by name, whatever their type
internal class RemoveKeys(private val keys: List<Preferences.Key<*>>) : DataMigration<Preferences> {
  override suspend fun shouldMigrate(currentData: Preferences): Boolean = keys.any {
    it in currentData
  }

  override suspend fun migrate(currentData: Preferences): Preferences {
    val prefs = currentData.toMutablePreferences()
    keys.forEach { prefs.remove(it) }
    return prefs.toPreferences()
  }

  override suspend fun cleanUp() = Unit
}
