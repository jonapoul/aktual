package aktual.core.model

import okio.ByteString

fun interface EncryptionKeys {
  suspend operator fun get(keyId: KeyId?): ByteString?
}
