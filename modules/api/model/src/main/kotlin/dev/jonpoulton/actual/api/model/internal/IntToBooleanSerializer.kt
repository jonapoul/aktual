package dev.jonpoulton.actual.api.model.internal

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

internal object IntToBooleanSerializer : KSerializer<Boolean> {
  override val descriptor = PrimitiveSerialDescriptor(serialName = "IntToBool", kind = PrimitiveKind.INT)

  override fun serialize(encoder: Encoder, value: Boolean) = encoder.encodeInt(if (value) 1 else 0)

  override fun deserialize(decoder: Decoder): Boolean = decoder.decodeInt() == 1
}
