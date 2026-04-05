package aktual.budget.model

import alakazam.kotlin.SerializableByString
import alakazam.kotlin.enumStringSerializer
import androidx.compose.runtime.Immutable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * See packages/loot-core/src/types/models/rule.ts
 *
 * [value] is a stupidly complex typescript type, and no idea what [queryFilter] is meant to be.
 * Both are [JsonElement] for now.
 */
@Immutable
@Serializable
data class Condition(
  @SerialName("field") val field: Field,
  @SerialName("op") val operator: Operator,
  @SerialName("value") val value: JsonElement,
  @SerialName("options") val options: ConditionOptions? = null,
  @SerialName("conditionsOp") val conditionsOp: ConditionOp? = null,
  @SerialName("type") val type: ConditionType? = null,
  @SerialName("customName") val customName: String? = null,
  @SerialName("queryFilter") val queryFilter: JsonElement? = null,
)

@Immutable
@Serializable
data class ConditionOptions(
  val inflow: Boolean? = null,
  val outflow: Boolean? = null,
  val month: Boolean? = null,
  val year: Boolean? = null,
)

@Serializable(ConditionOp.Serializer::class)
enum class ConditionOp(override val value: String) : SerializableByString {
  And("and"),
  Or("or");

  object Serializer : KSerializer<ConditionOp> by enumStringSerializer()
}

@Serializable(ConditionType.Serializer::class)
enum class ConditionType(override val value: String) : SerializableByString {
  Id("id"),
  Boolean("boolean"),
  Date("date"),
  Number("number"),
  String("string"),
  ImportedPayee("imported_payee");

  object Serializer : KSerializer<ConditionType> by enumStringSerializer()
}
