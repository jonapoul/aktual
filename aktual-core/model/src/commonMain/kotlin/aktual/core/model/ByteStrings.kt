package aktual.core.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import okio.ByteString
import okio.ByteString.Companion.decodeBase64

fun String.base64(): ByteString = requireNotNull(decodeBase64()) { "Failed decoding as b64: '$this'" }

typealias SerializableByteString = @Serializable(ByteStringSerializer::class) ByteString

object ByteStringSerializer : KSerializer<ByteString> {
  override val descriptor = PrimitiveSerialDescriptor("ByteString", PrimitiveKind.STRING)
  override fun deserialize(decoder: Decoder): ByteString = decoder.decodeString().base64()
  override fun serialize(encoder: Encoder, value: ByteString) = encoder.encodeString(value.base64())
}
