package aktual.budget.encryption

import aktual.core.model.KeyId
import okio.ByteString

fun interface EncryptionKeys {
  operator fun get(keyId: KeyId?): ByteString?
}
