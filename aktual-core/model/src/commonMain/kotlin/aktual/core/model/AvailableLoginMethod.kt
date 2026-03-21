package aktual.core.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Immutable
@Serializable
data class AvailableLoginMethod(
  @SerialName("method") val method: LoginMethod,
  @SerialName("displayName") val displayName: String,
  @SerialName("active") val isActive: @Serializable(IntToBoolSerializer::class) Boolean,
)

internal object IntToBoolSerializer : KSerializer<Boolean> {
  override val descriptor = Boolean.serializer().descriptor

  override fun deserialize(decoder: Decoder): Boolean = decoder.decodeInt() != 0

  override fun serialize(encoder: Encoder, value: Boolean) = encoder.encodeInt(if (value) 1 else 0)
}
