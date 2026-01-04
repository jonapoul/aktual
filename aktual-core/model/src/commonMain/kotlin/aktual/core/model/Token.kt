package aktual.core.model

import kotlinx.serialization.Serializable
import java.io.Serializable as JavaIoSerializable

@Serializable
@JvmInline
value class Token(val value: String) : JavaIoSerializable {
  override fun toString(): String = value
}
