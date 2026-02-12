package aktual.budget.model

import alakazam.kotlin.SerializableByString
import alakazam.kotlin.enumStringSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

@Serializable(Operator.Serializer::class)
enum class Operator(override val value: String) : SerializableByString {
  And("and"),
  Contains("contains"),
  DoesNotContain("doesNotContain"),
  GreaterThan("gt"),
  GreaterThanOrEquals("gte"),
  HasTags("hasTags"),
  Is("is"),
  IsApprox("isapprox"),
  IsBetween("isbetween"),
  IsNot("isNot"),
  LessThan("lt"),
  LessThanOrEquals("lte"),
  Matches("matches"),
  NotOneOf("notOneOf"),
  OffBudget("offBudget"),
  OnBudget("onBudget"),
  OneOf("oneOf");

  object Serializer : KSerializer<Operator> by enumStringSerializer()
}
