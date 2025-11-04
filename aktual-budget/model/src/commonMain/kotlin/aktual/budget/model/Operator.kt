/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.budget.model

import alakazam.kotlin.serialization.SerializableByString
import alakazam.kotlin.serialization.enumStringSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

@Serializable(OperatorSerializer::class)
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
  OneOf("oneOf"),
}

private object OperatorSerializer : KSerializer<Operator> by enumStringSerializer()
