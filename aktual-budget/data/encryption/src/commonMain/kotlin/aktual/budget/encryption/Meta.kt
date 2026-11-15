package aktual.budget.encryption

import aktual.core.model.KeyId
import dev.zacsweers.redacted.annotations.Redacted
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
  @Redacted override val iv: ByteString,
  @Redacted override val authTag: ByteString,
) : Meta
