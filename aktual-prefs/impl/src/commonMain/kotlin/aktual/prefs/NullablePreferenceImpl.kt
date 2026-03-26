package aktual.prefs

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

internal open class NullablePreferenceImpl<Encoded : Any, Decoded : Any>(
  private val dataStore: DataStore<Preferences>,
  private val key: Preferences.Key<Encoded>,
  override val default: Decoded?,
  private val translator: Translator<Encoded, Decoded>,
) : NullablePreference<Decoded> {
  override fun toString(): String = key.toString()

  override suspend fun get(): Decoded? =
    dataStore.data.map { prefs -> prefs[key]?.let(translator::decode) }.first() ?: default

  override suspend fun set(value: Decoded?) {
    dataStore.edit { prefs ->
      if (value == null) {
        prefs.remove(key)
      } else {
        prefs[key] = translator.encode(value)
      }
    }
  }

  override fun asFlow(): Flow<Decoded?> =
    dataStore.data
      .map { prefs -> prefs[key]?.let(translator::decode) ?: default }
      .distinctUntilChanged()
}

internal class SimpleNullablePreferenceImpl<T : Any>(
  dataStore: DataStore<Preferences>,
  key: Preferences.Key<T>,
  default: T?,
) : NullablePreferenceImpl<T, T>(dataStore, key, default, translator = NoOpTranslator())
