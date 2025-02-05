package actual.api.model.internal

import actual.api.model.account.LoginResponse
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
