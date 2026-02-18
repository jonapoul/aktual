package aktual.core.theme

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

internal object ColorLongSerializer : KSerializer<Color> {
  override val descriptor = PrimitiveSerialDescriptor("Color", PrimitiveKind.LONG)

  override fun serialize(encoder: Encoder, value: Color) = encoder.encodeLong(value.value.toLong())

  override fun deserialize(decoder: Decoder): Color = Color(decoder.decodeLong().toULong())
}
