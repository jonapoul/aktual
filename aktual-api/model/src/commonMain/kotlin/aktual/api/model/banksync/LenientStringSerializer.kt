package aktual.api.model.banksync

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.jsonPrimitive

/** Decodes a JSON string or number into its string form. */
internal object LenientStringSerializer : KSerializer<String> {
  override val descriptor = PrimitiveSerialDescriptor("LenientString", STRING)

  override fun deserialize(decoder: Decoder): String =
    (decoder as JsonDecoder).decodeJsonElement().jsonPrimitive.content

  override fun serialize(encoder: Encoder, value: String) = encoder.encodeString(value)
}
