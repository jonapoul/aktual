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
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonElement

/**
 * Same as [actual.budget.db.Categories], but serializable
 */
@Serializable
data class SelectedCategory(
  @SerialName("id") val id: CategoryId,
  @SerialName("name") val name: String,
  @SerialName("cat_group") val groupId: CategoryGroupId,
  @SerialName("is_income") val isIncome: BoolAsInt?,
  @SerialName("sort_order") val sortOrder: Double,
  @SerialName("goal_def") val goalDef: JsonElement? = null,
  @SerialName("hidden") val isHidden: BoolAsInt? = false,
  @SerialName("tombstone") val tombstone: BoolAsInt? = false,
)

private typealias BoolAsInt = @Serializable(IntToBoolSerializer::class) Boolean

private object IntToBoolSerializer : KSerializer<Boolean> {
  override val descriptor = Boolean.serializer().descriptor
  override fun deserialize(decoder: Decoder): Boolean = decoder.decodeInt() != 0
  override fun serialize(encoder: Encoder, value: Boolean) = encoder.encodeInt(if (value) 1 else 0)
}
