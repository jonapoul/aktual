package aktual.prefs

import aktual.core.model.EncryptionKeys
import aktual.core.model.KeyId
import kotlinx.coroutines.flow.Flow
import okio.ByteString

interface KeyPreferences : EncryptionKeys {
  suspend operator fun contains(keyId: KeyId?): Boolean

  override suspend operator fun get(keyId: KeyId?): ByteString?

  suspend operator fun set(keyId: KeyId, value: ByteString)

  suspend fun delete(keyId: KeyId)

  fun asFlow(keyId: KeyId): Flow<ByteString?>
}
