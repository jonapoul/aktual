package aktual.budget.encryption

import aktual.core.model.Base64String
import aktual.core.model.KeyId

interface Meta {
  val keyId: KeyId?
  val algorithm: String
  val iv: Base64String
  val authTag: Base64String
}
