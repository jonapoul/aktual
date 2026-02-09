package aktual.core.model

import java.io.Serializable as JavaIoSerializable
import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class Token(val value: String) : JavaIoSerializable {
  override fun toString(): String = value
}
