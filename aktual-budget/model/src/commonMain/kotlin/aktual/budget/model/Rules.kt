package aktual.budget.model

import alakazam.kotlin.SerializableByString
import alakazam.kotlin.enumStringSerializer
import androidx.compose.runtime.Immutable
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/** packages/loot-core/src/types/models/rule.ts */
@Immutable
@Serializable(RuleActionSerializer::class)
sealed interface RuleAction {
  @Serializable
  data class Set(
    @SerialName("field") val field: Field,
    @SerialName("value") val value: String, // one of the table ID classes
    @SerialName("options") val options: Options? = null,
    @SerialName("type") val type: String? = null,
    @SerialName("op") val op: Op = Op.Set,
  ) : RuleAction {
    @Serializable
    data class Options(
      @SerialName("template") val template: String? = null,
      @SerialName("formula") val formula: String? = null,
      @SerialName("splitIndex") val splitIndex: Int? = null,
    )
  }

  @Serializable
  data class SetSplitAmount(
    @SerialName("value") val value: Int? = null,
    @SerialName("options") val options: Options? = null,
    @SerialName("op") val op: Op = Op.SetSplitAmount,
  ) : RuleAction {
    @Serializable
    data class Options(
      @SerialName("splitIndex") val splitIndex: Int? = null,
      @SerialName("method") val method: Method,
      @SerialName("formula") val formula: String? = null,
    )

    @Serializable(Method.Serializer::class)
    enum class Method(override val value: String) : SerializableByString {
      FixedAmount("fixed-amount"),
      FixedPercent("fixed-percent"),
      Formula("formula"),
      Remainder("remainder");

      internal object Serializer : KSerializer<Method> by enumStringSerializer()
    }
  }

  @Serializable
  data class LinkSchedule(
    @SerialName("value") val value: ScheduleId,
    @SerialName("op") val op: Op = Op.LinkSchedule,
    @SerialName("field") val field: Field? = null,
    @SerialName("type") val type: String? = null,
  ) : RuleAction

  @Serializable
  data class PrependNotes(
    @SerialName("value") val value: String, // the string to prepend
    @SerialName("op") val op: Op = Op.PrependNotes,
  ) : RuleAction

  @Serializable
  data class AppendNotes(
    @SerialName("value") val value: String, // the string to append
    @SerialName("op") val op: Op = Op.AppendNotes,
  ) : RuleAction

  @Serializable
  data class DeleteTransaction(
    @SerialName("value") val value: String,
    @SerialName("op") val op: Op = Op.DeleteTransaction,
  ) : RuleAction

  @Serializable(Op.Serializer::class)
  enum class Op(override val value: String) : SerializableByString {
    Set("set"),
    SetSplitAmount("set-split-amount"),
    LinkSchedule("link-schedule"),
    PrependNotes("prepend-notes"),
    AppendNotes("append-notes"),
    DeleteTransaction("delete-transaction");

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

internal class RuleActionSerializer :
  JsonContentPolymorphicSerializer<RuleAction>(RuleAction::class) {
  override fun selectDeserializer(element: JsonElement): DeserializationStrategy<RuleAction> {
    val op = element.jsonObject.getValue("op").jsonPrimitive
    return when (Json.decodeFromJsonElement(RuleAction.Op.serializer(), op)) {
      RuleAction.Op.Set -> RuleAction.Set.serializer()
      RuleAction.Op.SetSplitAmount -> RuleAction.SetSplitAmount.serializer()
      RuleAction.Op.LinkSchedule -> RuleAction.LinkSchedule.serializer()
      RuleAction.Op.PrependNotes -> RuleAction.PrependNotes.serializer()
      RuleAction.Op.AppendNotes -> RuleAction.AppendNotes.serializer()
      RuleAction.Op.DeleteTransaction -> RuleAction.DeleteTransaction.serializer()
    }
  }
}
