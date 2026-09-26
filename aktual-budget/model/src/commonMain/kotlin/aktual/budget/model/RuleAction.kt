package aktual.budget.model

import androidx.compose.runtime.Immutable
import fallback.serializer.Fallback
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
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

  @Serializable
  enum class Method {
    @SerialName("fixed-amount") FixedAmount,
    @SerialName("fixed-percent") FixedPercent,
    @SerialName("formula") Formula,
    @SerialName("remainder") Remainder,
    @Fallback Unknown,
  }

  @Serializable
  enum class Op {
    @SerialName("set") Set, // value type is dependent on Field
    @SerialName("set-split-amount") SetSplitAmount, // value == int
    @SerialName("link-schedule") LinkSchedule, // value == ScheduleId
    @SerialName("prepend-notes") PrependNotes, // value == string to prepend
    @SerialName("append-notes") AppendNotes, // value == string to append
    @SerialName("delete-transaction") DeleteTransaction, // value == empty string
    @Fallback Unknown;

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
  @Serializable
  enum class Type {
    @SerialName("boolean") Boolean,
    @SerialName("date") Date,
    @SerialName("id") Id,
    @SerialName("number") Number,
    @SerialName("saved") Saved,
    @SerialName("string") String,
    @Fallback Unknown;

    companion object {
      val Default = Id
    }
  }
}

@Serializable
enum class RuleStage {
  @SerialName("pre") Pre,
  @SerialName("default") Default,
  @SerialName("post") Post,
  @Fallback Unknown;

  companion object {
    val known: ImmutableList<RuleStage> = entries.filter { it != Unknown }.toImmutableList()
  }
}
