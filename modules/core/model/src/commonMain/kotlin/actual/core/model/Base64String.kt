package actual.core.model

import kotlinx.serialization.Serializable
import kotlin.io.encoding.Base64

@JvmInline
@Serializable
value class Base64String(val value: String) : Comparable<Base64String> {
  constructor(bytes: ByteArray) : this(Base64.encode(bytes))

  override fun toString() = value
  override fun compareTo(other: Base64String) = value.compareTo(other.value)

  fun decode(): ByteArray = Base64.decode(value)
  fun toCharArray() = value.toCharArray()
  fun toByteArray() = value.toByteArray(Charsets.UTF_8)
}

val String.base64 get() = Base64String(this)
val ByteArray.base64 get() = Base64String(this)
