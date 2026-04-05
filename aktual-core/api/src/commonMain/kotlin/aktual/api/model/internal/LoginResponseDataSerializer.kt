package aktual.api.model.internal

import aktual.api.model.account.LoginResponse
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject

internal class LoginResponseDataSerializer :
  JsonContentPolymorphicSerializer<LoginResponse.Data>(LoginResponse.Data::class) {
  override fun selectDeserializer(element: JsonElement): KSerializer<out LoginResponse.Data> {
    val obj = element.jsonObject
    if ("returnUrl" in obj) return LoginResponse.Data.Redirect.serializer()
    return when (obj["token"]) {
      is JsonNull -> LoginResponse.Data.Invalid.serializer()
      is JsonPrimitive -> LoginResponse.Data.Valid.serializer()
      is JsonArray,
      is JsonObject,
      null -> error("Unknown response format: $element")
    }
  }
}
