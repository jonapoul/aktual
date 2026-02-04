package aktual.core.model

import okio.ByteString

fun interface EncryptionKeys {
  operator fun get(keyId: KeyId?): ByteString?
}
