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

import alakazam.kotlin.serialization.SerializableByString
import alakazam.kotlin.serialization.enumStringSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.JsonElement

/**
 * See packages/loot-core/src/types/models/rule.ts
 *
 * [value] is a stupidly complex typescript type, and no idea what [queryFilter] is meant to be. Both are [JsonElement]
 * for now.
 */
@Serializable
data class ReportCondition(
  @SerialName("field") val field: Field,
  @SerialName("op") val operator: Operator,
  @SerialName("value") val value: JsonElement,
  @SerialName("options") val options: Options? = null,
  @SerialName("conditionsOp") val conditionsOp: String? = null,
  @SerialName("type") val type: Type? = null,
  @SerialName("customName") val customName: String? = null,
  @SerialName("queryFilter") val queryFilter: JsonElement? = null,
) {
  @Serializable
  data class Options(
    val inflow: Boolean?,
    val outflow: Boolean?,
    val month: Boolean?,
    val year: Boolean?,
  )

  @Serializable(FieldSerializer::class)
  enum class Field(override val value: String) : SerializableByString {
    Account("account"),
    Amount("amount"),
    Category("category"),
    Date("date"),
    Notes("notes"),
    Payee("payee"),
    PayeeName("payee_name"),
    ImportedPayee("imported_payee"),
    Saved("saved"),
    Transfer("transfer"),
    Parent("parent"),
    Cleared("cleared"),
    Reconciled("reconciled"),
  }

  @Serializable(TypeSerializer::class)
  enum class Type(override val value: String) : SerializableByString {
    Id("id"),
    Boolean("boolean"),
    Date("date"),
    Number("number"),
    String("string"),
  }

  companion object {
    val ListSerializer = ListSerializer(serializer())

    private object FieldSerializer : KSerializer<Field> by enumStringSerializer()
    private object TypeSerializer : KSerializer<Type> by enumStringSerializer()
  }
}
