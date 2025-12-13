package aktual.budget.encryption

import aktual.core.model.Base64String
import aktual.core.model.KeyId

fun interface EncryptionKeys {
  operator fun get(keyId: KeyId?): Base64String?
}
