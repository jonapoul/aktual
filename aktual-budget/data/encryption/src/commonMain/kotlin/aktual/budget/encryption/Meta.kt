package aktual.budget.encryption

import aktual.core.model.KeyId
import okio.ByteString

interface Meta {
  val keyId: KeyId?
  val algorithm: String
  val iv: ByteString
  val authTag: ByteString
}

data class DefaultMeta(
  override val keyId: KeyId?,
  override val algorithm: String,
  override val iv: ByteString,
  override val authTag: ByteString,
) : Meta
