package aktual.core

import aktual.core.model.KeyId
import okio.ByteString

fun interface EncryptionKeys {
  suspend operator fun get(keyId: KeyId?): ByteString?
}
