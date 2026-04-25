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
  @SerialName("value") val value: JsonPrimitive?,
  @SerialName("op") val op: Op,
  @SerialName("field") val field: Field? = null,
  @SerialName("options") val options: Options? = null,
  @SerialName("type") val type: Type? = null,
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

    companion object {
      val Default = Set
    }
  }

  //  ┌─────────────────────┬───────────────────────────────────────────┬───────────────────────┐
  //  │          op         │                 type value                │              source   │
  //  ├─────────────────────┼───────────────────────────────────────────┼───────────────────────┤
  //  │                     │ FIELD_TYPES.get(field) → 'date' | 'id' |  │ action.ts:53-56 (from │
  //  │ set                 │ 'saved' | 'string' | 'number' | 'boolean' │ TYPE_INFO keys in     │
  //  │                     │                                           │ shared/rules.ts)      │
  //  ├─────────────────────┼───────────────────────────────────────────┼───────────────────────┤
  //  │ set-split-amount    │ 'number'                                  │ action.ts:70          │
  //  ├─────────────────────┼───────────────────────────────────────────┼───────────────────────┤
  //  │ link-schedule       │ 'id'                                      │ action.ts:73          │
  //  ├─────────────────────┼───────────────────────────────────────────┼───────────────────────┤
  //  │ prepend-notes /     │ 'id'                                      │ action.ts:76          │
  //  │ append-notes        │                                           │                       │
  //  ├─────────────────────┼───────────────────────────────────────────┼───────────────────────┤
  //  │ delete-transaction  │ unset (undefined)                         │ no branch sets it     │
  //  └─────────────────────┴───────────────────────────────────────────┴───────────────────────┘
  // Derived from FIELD_TYPES in packages/loot-core/src/shared/rules.ts; set by the
  // Action constructor in packages/loot-core/src/server/rules/action.ts.
  @Serializable(Type.Serializer::class)
  enum class Type(override val value: String) : SerializableByString {
    Boolean("boolean"),
    Date("date"),
    Id("id"),
    Number("number"),
    Saved("saved"),
    String("string");

    internal object Serializer : KSerializer<Type> by enumStringSerializer()

    companion object {
      val Default = Id
    }
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
