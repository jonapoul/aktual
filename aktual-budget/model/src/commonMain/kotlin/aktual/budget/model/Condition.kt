package aktual.budget.model

import androidx.compose.runtime.Immutable
import fallback.serializer.Fallback
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
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

@Serializable
enum class ConditionOp {
  @SerialName("and") And,
  @SerialName("or") Or,
  @Fallback Unknown;

  companion object {
    val Default = And
    val known: ImmutableList<ConditionOp> = entries.filter { it != Unknown }.toImmutableList()
  }
}

@Serializable
enum class ConditionType {
  @SerialName("id") Id,
  @SerialName("boolean") Boolean,
  @SerialName("date") Date,
  @SerialName("number") Number,
  @SerialName("string") String,
  @SerialName("imported_payee") ImportedPayee,
  @Fallback Unknown,
}
