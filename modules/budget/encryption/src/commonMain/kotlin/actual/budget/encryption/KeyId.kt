package actual.budget.encryption

import kotlin.uuid.Uuid

@JvmInline
value class KeyId(val value: String) : Comparable<KeyId> {
  constructor(uuid: Uuid) : this(uuid.toString())

  override fun toString(): String = value
  override fun compareTo(other: KeyId) = value.compareTo(other.value)

  companion object {
    fun random(): KeyId = KeyId(Uuid.Companion.random())
  }
}
