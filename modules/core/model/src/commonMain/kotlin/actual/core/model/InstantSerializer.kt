package actual.core.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.time.Instant

object InstantSerializer : KSerializer<Instant> {
  override val descriptor = PrimitiveSerialDescriptor("Instant", PrimitiveKind.STRING)

  override fun serialize(encoder: Encoder, value: Instant) = encoder.encodeString(value.toString())

  override fun deserialize(decoder: Decoder): Instant = decoder.decodeString().let(Instant::parse)
}
