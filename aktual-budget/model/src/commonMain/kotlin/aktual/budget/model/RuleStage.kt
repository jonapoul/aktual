/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.budget.model

import alakazam.kotlin.serialization.SerializableByString
import alakazam.kotlin.serialization.enumStringSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

@Serializable(RuleStage.Serializer::class)
enum class RuleStage(override val value: String) : SerializableByString {
  Pre(value = "pre"),
  Post(value = "post"),
  ;

  override fun toString(): String = value

  internal object Serializer : KSerializer<RuleStage> by enumStringSerializer()
}
