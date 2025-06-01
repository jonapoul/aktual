package actual.core.model

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

data class Key(
  val id: KeyId,
  val value: Base64String,
)

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
