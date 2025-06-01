package actual.core.model

import kotlinx.serialization.Serializable
import javax.crypto.SecretKey
import kotlin.uuid.Uuid

@JvmInline
@Serializable
value class KeyId(val value: String) : Comparable<KeyId> {
  constructor(uuid: Uuid) : this(uuid.toString())

  override fun toString(): String = value
  override fun compareTo(other: KeyId) = value.compareTo(other.value)

  companion object {
    fun random(): KeyId = KeyId(Uuid.Companion.random())
  }
}

data class Key(
  val raw: SecretKey,
  val base64: Base64String,
)
