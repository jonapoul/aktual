package aktual.budget.model

import alakazam.kotlin.SerializableByString
import alakazam.kotlin.enumStringSerializer
import androidx.compose.runtime.Immutable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
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
  @SerialName("options") val options: Options? = null,
  @SerialName("conditionsOp") val conditionsOp: Op? = null,
  @SerialName("type") val type: Type? = null,
  @SerialName("customName") val customName: String? = null,
  @SerialName("queryFilter") val queryFilter: JsonElement? = null,
) {
  @Serializable(Op.Serializer::class)
  enum class Op(override val value: String) : SerializableByString {
    And("and"),
    Or("or");

    object Serializer : KSerializer<Op> by enumStringSerializer()
  }

  @Immutable
  @Serializable
  data class Options(
    val inflow: Boolean?,
    val outflow: Boolean?,
    val month: Boolean?,
    val year: Boolean?,
  )

  @Serializable(Type.Serializer::class)
  enum class Type(override val value: String) : SerializableByString {
    Id("id"),
    Boolean("boolean"),
    Date("date"),
    Number("number"),
    String("string");

    object Serializer : KSerializer<Type> by enumStringSerializer()
  }

  companion object {
    val ListSerializer: KSerializer<List<Condition>> = ListSerializer(serializer())
  }
}
