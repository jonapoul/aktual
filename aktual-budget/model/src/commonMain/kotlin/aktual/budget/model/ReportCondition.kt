package aktual.budget.model

import alakazam.kotlin.SerializableByString
import alakazam.kotlin.enumStringSerializer
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

  @Serializable(Field.Serializer::class)
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
    ;

    object Serializer : KSerializer<Field> by enumStringSerializer()
  }

  @Serializable(Type.Serializer::class)
  enum class Type(override val value: String) : SerializableByString {
    Id("id"),
    Boolean("boolean"),
    Date("date"),
    Number("number"),
    String("string"),
    ;

    object Serializer : KSerializer<Type> by enumStringSerializer()
  }

  companion object {
    val ListSerializer: KSerializer<List<ReportCondition>> = ListSerializer(serializer())
  }
}
