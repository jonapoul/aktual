package actual.prefs

import actual.core.model.Base64String
import dev.jonpoulton.preferences.core.SimpleNullableStringSerializer
import javax.inject.Inject

class KeyPreferences @Inject constructor(private val prefs: EncryptedPreferences) {
  operator fun contains(keyId: String): Boolean = prefs.contains(key(keyId))
  operator fun get(keyId: String): Base64String? = preference(keyId).get()
  operator fun set(keyId: String, value: Base64String) = preference(keyId).set(value)

  fun delete(keyId: String) = preference(keyId).delete()
  fun asFlow(keyId: String) = preference(keyId).asFlow()

  private fun preference(keyId: String) = prefs.getNullableObject(key(keyId), Base64Serializer, default = null)
  private fun key(keyId: String) = "key-$keyId"

  private companion object {
    private val Base64Serializer = SimpleNullableStringSerializer { string -> string?.let(::Base64String) }
  }
}
