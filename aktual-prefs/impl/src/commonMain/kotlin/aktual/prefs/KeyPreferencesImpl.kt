package aktual.prefs

import aktual.core.model.EncryptionKeys
import aktual.core.model.KeyId
import aktual.core.model.base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.binding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import okio.ByteString

@ContributesBinding(AppScope::class, binding<KeyPreferences>())
@ContributesBinding(AppScope::class, binding<EncryptionKeys>())
class KeyPreferencesImpl(private val dataStore: DataStore<Preferences>) : KeyPreferences {
  override suspend operator fun contains(keyId: KeyId?): Boolean {
    val key = keyId?.let(::key) ?: return false
    return dataStore.data.map { prefs -> key in prefs }.first()
  }

  override suspend operator fun get(keyId: KeyId?): ByteString? {
    val key = keyId?.let(::key) ?: return null
    val b64 = dataStore.data.map { prefs -> prefs[key] }.first()
    return b64?.base64()
  }

  override suspend operator fun set(keyId: KeyId, value: ByteString) {
    dataStore.edit { prefs -> prefs[key(keyId)] = value.base64() }
  }

  override suspend fun delete(keyId: KeyId) {
    dataStore.edit { prefs -> prefs.remove(key(keyId)) }
  }

  override fun asFlow(keyId: KeyId): Flow<ByteString?> {
    val key = key(keyId)
    return dataStore.data.map { prefs -> prefs[key]?.base64() }
  }

  private fun key(keyId: KeyId) = stringPreferencesKey("key-$keyId")
}
