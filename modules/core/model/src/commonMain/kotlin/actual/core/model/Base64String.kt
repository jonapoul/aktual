package actual.core.model

import kotlinx.serialization.Serializable
import kotlin.io.encoding.Base64

@JvmInline
@Serializable
value class Base64String(val value: String) : Comparable<Base64String> {
  override fun toString() = value
  override fun compareTo(other: Base64String) = value.compareTo(other.value)
  fun decode(): ByteArray = Base64.decode(value)
}

val String.base64 get() = Base64String(this)
