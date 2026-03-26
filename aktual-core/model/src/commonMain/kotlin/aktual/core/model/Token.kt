package aktual.core.model

import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class Token(val value: String) {
  override fun toString(): String = value
}
