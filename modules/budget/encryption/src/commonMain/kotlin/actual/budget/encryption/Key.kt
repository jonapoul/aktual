package actual.budget.encryption

import actual.core.model.Base64String
import okio.ByteString

data class Key(
  val raw: ByteString,
  val base64: Base64String,
)
