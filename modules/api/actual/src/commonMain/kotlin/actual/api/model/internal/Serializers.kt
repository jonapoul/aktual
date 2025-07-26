package actual.api.model.internal

import actual.api.model.account.FailureReason
import actual.api.model.account.LoginResponse
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject

internal class LoginResponseDataSerializer :
  JsonContentPolymorphicSerializer<LoginResponse.Data>(LoginResponse.Data::class) {
  override fun selectDeserializer(element: JsonElement) = when (element.jsonObject["token"]) {
    is JsonNull -> LoginResponse.Data.Invalid.serializer()
    is JsonPrimitive -> LoginResponse.Data.Valid.serializer()
    else -> error("Unknown response format: $element")
  }
}

internal class FailureReasonSerializer : KSerializer<FailureReason> {
  override val descriptor = PrimitiveSerialDescriptor(serialName = "FailureReason", PrimitiveKind.STRING)

  override fun serialize(encoder: Encoder, value: FailureReason) = encoder.encodeString(value.reason)

  override fun deserialize(decoder: Decoder): FailureReason {
    val string = decoder.decodeString()
    return FailureReason.Known.entries.firstOrNull { it.reason == string } ?: FailureReason.Other(string)
  }
}

internal typealias BoolAsInt = @Serializable(IntToBoolSerializer::class) Boolean

internal object IntToBoolSerializer : KSerializer<Boolean> {
  override val descriptor = Boolean.serializer().descriptor
  override fun deserialize(decoder: Decoder): Boolean = decoder.decodeInt() != 0
  override fun serialize(encoder: Encoder, value: Boolean) = encoder.encodeInt(if (value) 1 else 0)
}
