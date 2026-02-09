package aktual.core.prefs

import aktual.core.model.EncryptionKeys
import aktual.core.model.KeyId
import aktual.core.model.base64
import dev.jonpoulton.preferences.core.NullableStringSerializer
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import okio.ByteString

@Inject
@ContributesBinding(AppScope::class, binding<KeyPreferences>())
@ContributesBinding(AppScope::class, binding<EncryptionKeys>())
class KeyPreferencesImpl(private val prefs: EncryptedPreferences) : KeyPreferences {
  override operator fun contains(keyId: KeyId?): Boolean =
      keyId?.let { prefs.contains(key(it)) } == true

  override operator fun get(keyId: KeyId?): ByteString? = keyId?.let { preference(it).get() }

  override operator fun set(keyId: KeyId, value: ByteString) = preference(keyId).set(value)

  override suspend fun setAndCommit(keyId: KeyId, value: ByteString) =
      preference(keyId).setAndCommit(value)

  override fun delete(keyId: KeyId) = preference(keyId).delete()

  override fun asFlow(keyId: KeyId) = preference(keyId).asFlow()

  private fun preference(keyId: KeyId) =
      prefs.getNullableObject(key(keyId), ByteStringSerializer, default = null)

  private fun key(keyId: KeyId) = "key-$keyId"

  private object ByteStringSerializer : NullableStringSerializer<ByteString> {
    override fun deserialize(value: String?): ByteString? = value?.base64()

    override fun serialize(value: ByteString?): String? = value?.base64()
  }
}
