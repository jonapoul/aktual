package actual.core.model

import kotlinx.serialization.Serializable
import java.io.Serializable as JavaIoSerializable

@Serializable
@JvmInline
value class LoginToken(val value: String) : JavaIoSerializable {
  override fun toString(): String = value
}
