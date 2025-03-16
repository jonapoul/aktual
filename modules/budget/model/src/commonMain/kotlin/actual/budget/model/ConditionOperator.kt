package actual.budget.model

import alakazam.kotlin.serialization.SerializableByString
import alakazam.kotlin.serialization.enumStringSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

@Serializable(ConditionOperator.Serializer::class)
enum class ConditionOperator(override val value: String) : SerializableByString {
  And(value = "and"),
  Contains(value = "contains"),
  DoesNotContains(value = "doesNotContain"),
  Gt(value = "gt"),
  Gte(value = "gte"),
  HasTags(value = "hasTags"),
  Is(value = "is"),
  IsApprox(value = "isapprox"),
  IsBetween(value = "isbetween"),
  IsNot(value = "isNot"),
  Lt(value = "lt"),
  Lte(value = "lte"),
  Matches(value = "matches"),
  NotOneOf(value = "notOneOf"),
  OffBudget(value = "offBudget"),
  OnBudget(value = "onBudget"),
  OneOf(value = "oneOf"),
  ;

  override fun toString(): String = value

  internal object Serializer : KSerializer<ConditionOperator> by enumStringSerializer()
}
