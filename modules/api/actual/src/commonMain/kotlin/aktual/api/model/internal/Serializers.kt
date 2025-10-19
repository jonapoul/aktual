/**
 * Copyright 2025 Jon Poulton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package aktual.api.model.internal

import aktual.api.model.account.LoginResponse
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
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

internal typealias BoolAsInt = @Serializable(IntToBoolSerializer::class) Boolean

internal object IntToBoolSerializer : KSerializer<Boolean> {
  override val descriptor = Boolean.serializer().descriptor
  override fun deserialize(decoder: Decoder): Boolean = decoder.decodeInt() != 0
  override fun serialize(encoder: Encoder, value: Boolean) = encoder.encodeInt(if (value) 1 else 0)
}
