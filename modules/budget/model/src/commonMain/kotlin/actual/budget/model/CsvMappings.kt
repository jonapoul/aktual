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
package actual.budget.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
data class CsvMappings(
  @SerialName("date") @Serializable(with = StringIntSerializer::class) val date: Int,
  @SerialName("amount") @Serializable(with = StringIntSerializer::class) val amount: Int,
  @SerialName("payee") @Serializable(with = StringIntSerializer::class) val payee: Int,
  @SerialName("notes") val notes: Int,
  @SerialName("inOut") val inOut: Int,
  @SerialName("category") @Serializable(with = StringIntSerializer::class) val category: Int,
)

private object StringIntSerializer : KSerializer<Int> {
  override val descriptor = PrimitiveSerialDescriptor(serialName = "Int", PrimitiveKind.STRING)
  override fun deserialize(decoder: Decoder): Int = decoder.decodeString().toInt()
  override fun serialize(encoder: Encoder, value: Int) = encoder.encodeString(value.toString())
}
