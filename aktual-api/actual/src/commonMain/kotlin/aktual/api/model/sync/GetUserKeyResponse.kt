package aktual.api.model.sync

import aktual.api.model.account.FailureReason
import aktual.core.model.KeyId
import aktual.core.model.SerializableByteString
import aktual.core.model.base64
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

sealed interface GetUserKeyResponse {
  @Serializable(Serializer::class)
  data class Success(
    @SerialName("data") val data: Data,
  ) : GetUserKeyResponse

  @Serializable
  data class Failure(
    @SerialName("reason") val reason: FailureReason,
    @SerialName("details") val details: String? = null,
  ) : GetUserKeyResponse

  @Serializable
  data class Data(
    @SerialName("id") val id: KeyId,
    @SerialName("salt") val salt: SerializableByteString,
    @SerialName("test") val test: Test,
  )

  @Serializable
  data class Test(
    @SerialName("value") val value: SerializableByteString,
    @SerialName("meta") val meta: EncryptMeta,
  )
}

private object Serializer : KSerializer<GetUserKeyResponse.Success> {
  override val descriptor: SerialDescriptor = JsonObject.serializer().descriptor

  override fun serialize(encoder: Encoder, value: GetUserKeyResponse.Success) = error("Don't need to serialize")

  override fun deserialize(decoder: Decoder): GetUserKeyResponse.Success {
    val jsonDecoder = decoder as JsonDecoder
    val root = jsonDecoder.decodeJsonElement().jsonObject
    val data = root["data"]?.jsonObject ?: throw SerializationException("Null data in $root")
    val test = data.string("test").let {
      jsonDecoder.json.decodeFromString(GetUserKeyResponse.Test.serializer(), it)
    }
    return GetUserKeyResponse.Success(
      data = GetUserKeyResponse.Data(
        id = KeyId(data.string("id")),
        salt = data.string("salt").base64(),
        test = test.requireNotNull(),
      ),
    )
  }

  private fun JsonObject.string(key: String): String =
    get(key)?.jsonPrimitive?.contentOrNull ?: throw SerializationException("Null value of $key in $this")

  private fun <T> T?.requireNotNull(): T = this ?: throw SerializationException("Null element")
}
