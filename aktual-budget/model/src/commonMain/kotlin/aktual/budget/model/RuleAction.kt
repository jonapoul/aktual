package aktual.budget.model

import alakazam.kotlin.SerializableByString
import alakazam.kotlin.enumStringSerializer
import androidx.compose.runtime.Immutable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonPrimitive

/** packages/loot-core/src/types/models/rule.ts */
@Immutable
@Serializable
data class RuleAction(
  @SerialName("value") val value: JsonPrimitive,
  @SerialName("op") val op: Op,
  @SerialName("field") val field: Field? = null,
  @SerialName("options") val options: Options? = null,
  @SerialName("type") val type: String? = null,
) {
  @Serializable
  data class Options(
    @SerialName("template") val template: String? = null,
    @SerialName("formula") val formula: String? = null,
    @SerialName("method") val method: Method? = null,
    @SerialName("splitIndex") val splitIndex: Int? = null,
  )

  @Serializable(Method.Serializer::class)
  enum class Method(override val value: String) : SerializableByString {
    FixedAmount("fixed-amount"),
    FixedPercent("fixed-percent"),
    Formula("formula"),
    Remainder("remainder");

    internal object Serializer : KSerializer<Method> by enumStringSerializer()
  }

  @Serializable(Op.Serializer::class)
  enum class Op(override val value: String) : SerializableByString {
    Set("set"), // value type is dependent on Field
    SetSplitAmount("set-split-amount"), // value == int
    LinkSchedule("link-schedule"), // value == ScheduleId
    PrependNotes("prepend-notes"), // value == string to prepend
    AppendNotes("append-notes"), // value == string to append
    DeleteTransaction("delete-transaction"); // value == empty string

    internal object Serializer : KSerializer<Op> by enumStringSerializer()
  }
}

@Serializable(RuleStage.Serializer::class)
enum class RuleStage(override val value: String) : SerializableByString {
  Pre(value = "pre"),
  Default(value = "default"),
  Post(value = "post");

  override fun toString(): String = value

  internal object Serializer : KSerializer<RuleStage> by enumStringSerializer()
}
