package aktual.core.prefs

import aktual.core.model.EncryptionKeys
import aktual.core.model.KeyId
import kotlinx.coroutines.flow.Flow
import okio.ByteString

interface KeyPreferences : EncryptionKeys {
  operator fun contains(keyId: KeyId?): Boolean
  override operator fun get(keyId: KeyId?): ByteString?
  operator fun set(keyId: KeyId, value: ByteString)

  suspend fun setAndCommit(keyId: KeyId, value: ByteString): Boolean

  fun delete(keyId: KeyId)
  fun asFlow(keyId: KeyId): Flow<ByteString?>
}
