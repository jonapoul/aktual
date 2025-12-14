package aktual.prefs

import aktual.budget.encryption.EncryptionKeys
import aktual.core.model.KeyId
import aktual.core.model.base64
import dev.jonpoulton.preferences.core.NullableStringSerializer
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import okio.ByteString

@Inject
@ContributesBinding(AppScope::class, binding<EncryptionKeys>())
class KeyPreferences(private val prefs: EncryptedPreferences) : EncryptionKeys {
  operator fun contains(keyId: KeyId?): Boolean = keyId?.let { prefs.contains(key(it)) } == true
  override operator fun get(keyId: KeyId?): ByteString? = keyId?.let { preference(it).get() }
  operator fun set(keyId: KeyId, value: ByteString) = preference(keyId).set(value)

  suspend fun setAndCommit(keyId: KeyId, value: ByteString) = preference(keyId).setAndCommit(value)

  fun delete(keyId: KeyId) = preference(keyId).delete()
  fun asFlow(keyId: KeyId) = preference(keyId).asFlow()

  private fun preference(keyId: KeyId) = prefs.getNullableObject(key(keyId), ByteStringSerializer, default = null)
  private fun key(keyId: KeyId) = "key-$keyId"

  private object ByteStringSerializer : NullableStringSerializer<ByteString> {
    override fun deserialize(value: String?): ByteString? = value?.base64()
    override fun serialize(value: ByteString?): String? = value?.base64()
  }
}
