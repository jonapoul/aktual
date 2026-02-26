package aktual.core.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

internal object ColorSerializer : KSerializer<Color> {
  override val descriptor = PrimitiveSerialDescriptor("Color", PrimitiveKind.STRING)

  @Suppress("MagicNumber")
  override fun serialize(encoder: Encoder, value: Color) {
    // toArgb() returns AARRGGBB; parseHex 8-char branch expects CSS RRGGBBAA
    val argb = value.toArgb()
    val aa = argb ushr 24 and 0xFF
    val rrggbb = argb and 0xFFFFFF
    val rrggbbaa = (rrggbb shl 8) or aa
    encoder.encodeString("#%08X".format(rrggbbaa))
  }

  override fun deserialize(decoder: Decoder): Color = decoder.decodeString().parseColor()
}
