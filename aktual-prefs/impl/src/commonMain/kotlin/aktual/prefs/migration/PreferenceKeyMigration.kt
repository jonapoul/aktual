package aktual.prefs.migration

import androidx.datastore.core.DataMigration
import androidx.datastore.preferences.core.Preferences

// Moves a value from oldKey to newKey, converting it on the way. Returning null from convert drops
// the old value, so the new preference falls back to its default. The old key is always removed.
internal abstract class PreferenceKeyMigration<Old : Any, New : Any>(
  private val oldKey: Preferences.Key<Old>,
  private val newKey: Preferences.Key<New>,
) : DataMigration<Preferences> {
  init {
    require(oldKey.name != newKey.name) { "Keys must differ, got ${oldKey.name} for both" }
  }

  abstract fun convert(old: Old): New?

  override suspend fun shouldMigrate(currentData: Preferences): Boolean = oldKey in currentData

  override suspend fun migrate(currentData: Preferences): Preferences {
    val prefs = currentData.toMutablePreferences()
    val old = prefs.remove(oldKey)
    if (newKey !in prefs) {
      convert(old)?.let { prefs[newKey] = it }
    }
    return prefs.toPreferences()
  }

  override suspend fun cleanUp() = Unit
}
