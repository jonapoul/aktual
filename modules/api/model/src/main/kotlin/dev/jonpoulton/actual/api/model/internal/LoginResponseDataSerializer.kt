package dev.jonpoulton.actual.api.model.internal

import dev.jonpoulton.actual.api.model.account.LoginResponse
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

internal class LoginResponseDataSerializer :
  JsonContentPolymorphicSerializer<LoginResponse.Data>(LoginResponse.Data::class) {
  override fun selectDeserializer(element: JsonElement): DeserializationStrategy<LoginResponse.Data> {
    val token = element.jsonObject["token"]?.jsonPrimitive
    return if (token == null) {
      LoginResponse.Data.Invalid.serializer()
    } else {
      LoginResponse.Data.Valid.serializer()
    }
  }
}
